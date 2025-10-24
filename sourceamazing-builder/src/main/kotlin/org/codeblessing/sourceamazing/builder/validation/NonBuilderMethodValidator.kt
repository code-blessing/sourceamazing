package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.full.valueParameters
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.factories.BuilderFactoriesHolder
import org.codeblessing.sourceamazing.builder.interpretation.BuilderMethodInterpreter
import org.codeblessing.sourceamazing.builder.validation.BuilderAnnotationValidationUtil.hasBuilderAnnotation

object NonBuilderMethodValidator {

    fun validateNonBuilderMethod(
        builderMethodInterpreter: BuilderMethodInterpreter,
        builderFactoriesHolder: BuilderFactoriesHolder,
    ) {
        val nonBuilderMethodValidationData =
            nonBuilderMethodValidationData(builderMethodInterpreter, builderFactoriesHolder)

        checkThatBuilderHasImplementationClassOrIsDefaultMethod(nonBuilderMethodValidationData)
        checkThatNonBuilderMethodHaveNoBuilderAnnotations(nonBuilderMethodValidationData)
    }

    private fun checkThatBuilderHasImplementationClassOrIsDefaultMethod(
        nonBuilderMethodValidationData: NonBuilderMethodValidationData
    ) {
        val builderMethodInterpreter = nonBuilderMethodValidationData.builderMethodInterpreter
        val builderClass = builderMethodInterpreter.builderClassInterpreter.builderClass
        val builderFactoriesHolder = nonBuilderMethodValidationData.builderFactoriesHolder

        if (!builderMethodInterpreter.isDefaultMethod() && !builderFactoriesHolder.hasImplementation(builderClass)) {
            throw BuilderMethodSyntaxException(
                builderMethodInterpreter.methodLocation,
                BuilderErrorCode.BUILDER_WITH_NON_BUILDER_METHODS_MUST_HAVE_BUILDER_IMPLEMENTATION
                    .withFormattedMessage(),
            )
        }
    }

    private fun checkThatNonBuilderMethodHaveNoBuilderAnnotations(
        nonBuilderMethodValidationData: NonBuilderMethodValidationData
    ) {
        val methodLocation = nonBuilderMethodValidationData.builderMethodInterpreter.methodLocation
        val method = nonBuilderMethodValidationData.builderMethodInterpreter.method

        method.checkHasNoBuilderAnnotations(methodLocation)
        method.valueParameters.forEach { parameter ->
            parameter.checkHasNoBuilderAnnotations(methodLocation.extendWithMethodParam(parameter))
        }
    }

    private fun KAnnotatedElement.checkHasNoBuilderAnnotations(methodLocation: MethodLocation) {
        if (hasBuilderAnnotation(this)) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.NON_BUILDER_METHODS_CAN_NOT_HAVE_BUILDER_ANNOTATIONS.withFormattedMessage(),
            )
        }
    }

    private fun nonBuilderMethodValidationData(
        builderMethodInterpreter: BuilderMethodInterpreter,
        builderFactoriesHolder: BuilderFactoriesHolder,
    ): NonBuilderMethodValidationData {
        return NonBuilderMethodValidationData(builderMethodInterpreter, builderFactoriesHolder)
    }

    private class NonBuilderMethodValidationData(
        val builderMethodInterpreter: BuilderMethodInterpreter,
        val builderFactoriesHolder: BuilderFactoriesHolder,
    )
}
