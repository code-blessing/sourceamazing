package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters
import org.codeblessing.sourceamazing.builder.interpretation.BuilderMethodInterpreter

object BuilderMethodParameterValidator {

    fun validateBuilderMethodParameter(builderMethodInterpreter: BuilderMethodInterpreter) {
        forEachValueParameter(builderMethodInterpreter) { methodParameter ->
            validateBuilderMethodParameter(methodParameter)
        }
    }

    private fun forEachValueParameter(
        builderMethodInterpreter: BuilderMethodInterpreter,
        block: (parameter: BuilderMethodParameterValidationData) -> Unit,
    ) {
        val method = builderMethodInterpreter.method

        method.valueParameters.forEachIndexed { parameterIndex, methodParameter ->
            val parameterValidationData =
                builderMethodParameterValidationData(builderMethodInterpreter, parameterIndex, methodParameter)
            block(parameterValidationData)
        }
    }

    private fun validateBuilderMethodParameter(methodParameter: BuilderMethodParameterValidationData) {
        validateIgnoreNullClazzPropertyValueMethodParameterAnnotation(methodParameter)
        validateInjectBuilderMethodParameterAnnotations(methodParameter)
        validateExpectedMethodParameterAnnotations(methodParameter)
        validateInjectBuilderMethodParamType(methodParameter)
    }

    private fun validateExpectedMethodParameterAnnotations(methodParameter: BuilderMethodParameterValidationData) {
        with(methodParameter.builderMethodParameterChecker()) { checkParameterAllowedParameterAnnotations() }
    }

    private fun validateIgnoreNullClazzPropertyValueMethodParameterAnnotation(
        methodParameter: BuilderMethodParameterValidationData
    ) {
        with(methodParameter.builderMethodParameterChecker()) {
            checkNotIgnoreClazzPropertyValueAnnotationAndInjectBuilderAnnotationTogether()
        }
    }

    private fun validateInjectBuilderMethodParameterAnnotations(methodParameter: BuilderMethodParameterValidationData) {
        with(methodParameter.builderMethodParameterChecker()) { checkInjectBuilderAnnotationIsLastParameter() }
    }

    private fun validateInjectBuilderMethodParamType(methodParameter: BuilderMethodParameterValidationData) {
        with(methodParameter.builderMethodParameterChecker()) {
            checkInjectBuilderIsNotNullable()
            checkInjectBuilderHasNoReturnType()
            checkInjectBuilderIsReceiverParameterAndHasNoValueParameters()
            checkInjectBuilderReceiverParameterIsValidType()
            checkInjectBuilderReceiverParameterIsNotNullable()
            checkInjectBuilderReceiverParameterIsClass()
        }
    }

    private fun builderMethodParameterValidationData(
        builderMethodInterpreter: BuilderMethodInterpreter,
        parameterIndex: Int,
        valueParameter: KParameter,
    ): BuilderMethodParameterValidationData {
        return BuilderMethodParameterValidationData(
            builderMethodInterpreter = builderMethodInterpreter,
            parameterIndex = parameterIndex,
            methodParameter = valueParameter,
        )
    }

    private class BuilderMethodParameterValidationData(
        private val builderMethodInterpreter: BuilderMethodInterpreter,
        parameterIndex: Int,
        private val methodParameter: KParameter,
    ) {
        val numberOfParameters = builderMethodInterpreter.method.valueParameters.size
        val isLastParameter = parameterIndex == (numberOfParameters - 1)

        fun builderMethodParameterChecker(): BuilderMethodParameterChecker {
            return BuilderMethodParameterChecker(
                methodParameter = methodParameter,
                isLastParameter = isLastParameter,
                methodLocation = builderMethodInterpreter.methodLocation,
            )
        }
    }
}
