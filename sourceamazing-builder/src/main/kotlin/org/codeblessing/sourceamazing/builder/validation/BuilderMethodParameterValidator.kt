package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.api.annotations.ProvideBuilderData
import org.codeblessing.sourceamazing.builder.interpretation.BuilderDataProviderInterpreter
import org.codeblessing.sourceamazing.builder.interpretation.BuilderMethodInterpreter
import org.codeblessing.sourceamazing.schema.api.SchemaAccess
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.valueParameters

object BuilderMethodParameterValidator {

    fun validateBuilderMethodParameter(builderMethodInterpreter: BuilderMethodInterpreter, schemaAccess: SchemaAccess) {
        forEachValueParameter(builderMethodInterpreter, schemaAccess) { methodParameter ->
            validateBuilderMethodParameter(methodParameter)
            validateBuilderDataProvider(methodParameter)
        }
    }

    private fun forEachValueParameter(
        builderMethodInterpreter: BuilderMethodInterpreter,
        schemaAccess: SchemaAccess,
        block: (parameter: BuilderMethodParameterValidationData) -> Unit,
    ) {
        val method = builderMethodInterpreter.method

        method.valueParameters.forEachIndexed { parameterIndex, methodParameter ->
            val parameterValidationData =
                builderMethodParameterValidationData(builderMethodInterpreter, parameterIndex, methodParameter, schemaAccess)
            block(parameterValidationData)
        }
    }

    private fun validateBuilderMethodParameter(methodParameter: BuilderMethodParameterValidationData) {
        validateIgnoreNullFacetValueMethodParameterAnnotation(methodParameter)
        validateInjectBuilderMethodParameterAnnotations(methodParameter)
        validateExpectedMethodParameterAnnotations(methodParameter)
        validateInjectBuilderMethodParamType(methodParameter)
        validateProvideBuilderDataMethodParameterAnnotations(methodParameter)
    }

    private fun validateProvideBuilderDataMethodParameterAnnotations(
        methodParameter: BuilderMethodParameterValidationData
    ) {
        with(methodParameter.builderMethodParameterChecker()) {
            checkDataProviderParameterIsNotNullable()
            checkDataProviderParameterIsNotReceiverParameter()
            checkDataProviderParameterIsClass()
            checkDataProviderParameterValidClassType()
        }
    }

    private fun validateExpectedMethodParameterAnnotations(methodParameter: BuilderMethodParameterValidationData) {
        with(methodParameter.builderMethodParameterChecker()) { checkParameterAllowedParameterAnnotations() }
    }

    private fun validateIgnoreNullFacetValueMethodParameterAnnotation(
        methodParameter: BuilderMethodParameterValidationData
    ) {
        with(methodParameter.builderMethodParameterChecker()) {
            checkNotIgnoreFacetValueAnnotationAndInjectBuilderAnnotationTogether()
            checkNotIgnoreFacetValueAnnotationAndProvideBuilderDataAnnotationTogether()
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

    private fun validateBuilderDataProvider(methodParameter: BuilderMethodParameterValidationData) {
        if (methodParameter.hasProvideBuilderDataAnnotation()) {
            BuilderDataProviderHierarchyValidator.validateTopLevelBuilderDataProvider(
                builderDataProviderInterpreter = methodParameter.builderDataProviderInterpreter()
            )
        }
    }

    private fun builderMethodParameterValidationData(
        builderMethodInterpreter: BuilderMethodInterpreter,
        parameterIndex: Int,
        valueParameter: KParameter,
        schemaAccess: SchemaAccess,
    ): BuilderMethodParameterValidationData {
        return BuilderMethodParameterValidationData(
            builderMethodInterpreter = builderMethodInterpreter,
            parameterIndex = parameterIndex,
            methodParameter = valueParameter,
            schemaAccess = schemaAccess,
        )
    }

    private class BuilderMethodParameterValidationData(
        private val builderMethodInterpreter: BuilderMethodInterpreter,
        parameterIndex: Int,
        private val methodParameter: KParameter,
        private val schemaAccess: SchemaAccess,
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

        fun builderDataProviderInterpreter(): BuilderDataProviderInterpreter {
            return BuilderDataProviderInterpreter.createFromMethodParam(
                methodParameter = methodParameter,
                builderMethodInterpreter = builderMethodInterpreter,
                schemaAccess = schemaAccess,
            )
        }

        fun hasProvideBuilderDataAnnotation(): Boolean {
            return methodParameter.hasAnnotation<ProvideBuilderData>()
        }
    }
}
