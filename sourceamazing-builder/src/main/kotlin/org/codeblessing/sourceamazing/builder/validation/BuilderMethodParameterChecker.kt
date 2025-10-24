package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.hasAnnotation
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.utils.type.KTypeUtil
import org.codeblessing.sourceamazing.schema.utils.type.receiverParameter
import org.codeblessing.sourceamazing.schema.utils.type.returnTypeOrNull
import org.codeblessing.sourceamazing.schema.utils.type.valueParameters

class BuilderMethodParameterChecker(
    private val methodParameter: KParameter,
    private val isLastParameter: Boolean,
    private val methodLocation: MethodLocation,
) {

    fun checkNotIgnoreClazzPropertyValueAnnotationAndInjectBuilderAnnotationTogether() {
        if (methodParameter.hasAnnotation<IgnoreNullValue>()) {
            if (methodParameter.hasAnnotation<InjectBuilder>()) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCodeWithMessage =
                        BuilderErrorCode.BUILDER_PARAM_INJECTION_AND_IGNORE_NULL_ANNOTATION.withFormattedMessage(),
                )
            }
        }
    }

    fun checkInjectBuilderAnnotationIsLastParameter() {
        if (methodParameter.hasAnnotation<InjectBuilder>()) {
            if (!isLastParameter) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCodeWithMessage =
                        BuilderErrorCode.BUILDER_PARAM_ONLY_LAST_PARAM_CAN_BE_INJECTION.withFormattedMessage(),
                )
            }
        }
    }

    fun checkInjectBuilderIsNotNullable() {
        if (methodParameter.hasAnnotation<InjectBuilder>()) {
            val injectionBuilderKType = methodParameter.type
            if (injectionBuilderKType.isMarkedNullable) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCodeWithMessage =
                        BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_BE_NULLABLE.withFormattedMessage(),
                )
            }
        }
    }

    fun checkInjectBuilderHasNoReturnType() {
        if (methodParameter.hasAnnotation<InjectBuilder>()) {
            val injectionBuilderKType = methodParameter.type

            if (injectionBuilderKType.returnTypeOrNull() != null) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCodeWithMessage =
                        BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_HAVE_RETURN_TYPE.withFormattedMessage(),
                )
            }
        }
    }

    fun checkInjectBuilderIsReceiverParameterAndHasNoValueParameters() {
        if (methodParameter.hasAnnotation<InjectBuilder>()) {
            val injectionBuilderKType = methodParameter.type

            val receiverParameterType = injectionBuilderKType.receiverParameter()

            if (receiverParameterType == null || injectionBuilderKType.valueParameters().isNotEmpty()) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCodeWithMessage =
                        BuilderErrorCode.BUILDER_PARAM_INJECTION_PARAMS_INVALID.withFormattedMessage(),
                )
            }
        }
    }

    private fun injectBuilderReceiverParameterKType(): KType? {
        if (methodParameter.hasAnnotation<InjectBuilder>()) {
            val injectionBuilderKType = methodParameter.type

            val receiverParameterType = injectionBuilderKType.receiverParameter()
            if (receiverParameterType != null) {
                return try {
                    KTypeUtil.kTypeFromProjection(receiverParameterType)
                } catch (ex: IllegalStateException) {
                    throw BuilderMethodSyntaxException(
                        methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                        errorCodeWithMessage =
                            BuilderErrorCode.BUILDER_PARAM_INJECTION_INVALID_RECEIVER_PARAM.withFormattedMessage(
                                ex.message ?: ""
                            ),
                    )
                }
            }
        }
        return null
    }

    fun checkInjectBuilderReceiverParameterIsValidType() {
        injectBuilderReceiverParameterKType()
    }

    fun checkInjectBuilderReceiverParameterIsNotNullable() {
        if (methodParameter.hasAnnotation<InjectBuilder>()) {
            val receiverParameterKType = injectBuilderReceiverParameterKType()
            if (receiverParameterKType != null) {
                if (receiverParameterKType.isMarkedNullable) {
                    throw BuilderMethodSyntaxException(
                        methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                        errorCodeWithMessage =
                            BuilderErrorCode.BUILDER_PARAM_INJECTION_NOT_NULLABLE_RECEIVER_PARAM.withFormattedMessage(),
                    )
                }
            }
        }
    }

    fun checkInjectBuilderReceiverParameterIsClass() {
        if (methodParameter.hasAnnotation<InjectBuilder>()) {
            val receiverParameterKType = injectBuilderReceiverParameterKType()
            if (receiverParameterKType != null) {
                try {
                    KTypeUtil.classFromType(receiverParameterKType)
                } catch (ex: IllegalStateException) {
                    throw BuilderMethodSyntaxException(
                        methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                        errorCodeWithMessage =
                            BuilderErrorCode.BUILDER_PARAM_INJECTION_INVALID_RECEIVER_PARAM.withFormattedMessage(
                                ex.message ?: ""
                            ),
                    )
                }
            }
        }
    }

    fun checkParameterAllowedParameterAnnotations() {
        val hasAllowedMethodAnnotations =
            methodParameter.hasAnnotation<SetAsClazzModelId>() ||
                methodParameter.hasAnnotation<SetAsValue>() ||
                methodParameter.hasAnnotation<SetClazzModelOfId>()
        if (!isLastParameter) {
            if (!hasAllowedMethodAnnotations) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCodeWithMessage =
                        BuilderErrorCode.BUILDER_PARAM_MISSING_CLAZZ_IDENTIFIER_OR_SET_CLAZZ_PROPERTY_ANNOTATION
                            .withFormattedMessage(),
                )
            }
        } else {
            if (!hasAllowedMethodAnnotations && !methodParameter.hasAnnotation<InjectBuilder>()) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCodeWithMessage =
                        BuilderErrorCode
                            .BUILDER_PARAM_MISSING_CLAZZ_IDENTIFIER_OR_SET_CLAZZ_PROPERTY_ANNOTATION_OR_INJECTION
                            .withFormattedMessage(),
                )
            }
        }
    }
}
