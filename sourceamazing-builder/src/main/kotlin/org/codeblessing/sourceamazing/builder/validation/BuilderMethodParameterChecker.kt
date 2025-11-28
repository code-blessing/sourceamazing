package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.hasAnnotation
import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.utils.type.*

class BuilderMethodParameterChecker(
    private val methodParameter: KParameter,
    private val isLastParameter: Boolean,
    private val methodLocation: MethodLocation,
) {

    fun checkNotIgnoreFacetValueAnnotationAndInjectBuilderAnnotationTogether() {
        if (methodParameter.hasAnnotation<IgnoreNullFacetValue>()) {
            if (methodParameter.hasAnnotation<InjectBuilder>()) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_AND_IGNORE_NULL_ANNOTATION,
                )
            }
        }
    }

    fun checkNotIgnoreFacetValueAnnotationAndProvideBuilderDataAnnotationTogether() {
        if (methodParameter.hasAnnotation<IgnoreNullFacetValue>()) {
            if (methodParameter.hasAnnotation<ProvideBuilderData>()) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_DATA_PROVIDER_AND_IGNORE_NULL_ANNOTATION,
                )
            }
        }
    }

    fun checkInjectBuilderAnnotationIsLastParameter() {
        if (methodParameter.hasAnnotation<InjectBuilder>()) {
            if (!isLastParameter) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_ONLY_LAST_PARAM_CAN_BE_INJECTION,
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
                    errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_BE_NULLABLE,
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
                    errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_HAVE_RETURN_TYPE,
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
                    errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_PARAMS_INVALID,
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
                        errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_INVALID_RECEIVER_PARAM,
                        ex.message ?: "",
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
                        errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_NOT_NULLABLE_RECEIVER_PARAM,
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
                        errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_INVALID_RECEIVER_PARAM,
                        ex.message ?: "",
                    )
                }
            }
        }
    }

    fun checkDataProviderParameterIsNotNullable() {
        if (methodParameter.hasAnnotation<ProvideBuilderData>()) {
            val builderDataProviderKType = methodParameter.type
            if (builderDataProviderKType.isMarkedNullable) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_DATA_PROVIDER_CANNOT_BE_NULLABLE,
                )
            }
        }
    }

    fun checkDataProviderParameterIsNotReceiverParameter() {
        if (methodParameter.hasAnnotation<ProvideBuilderData>()) {
            val builderDataProviderKType = methodParameter.type
            if (builderDataProviderKType.receiverParameter() != null) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_DATA_PROVIDER_PARAMS_INVALID,
                )
            }
        }
    }

    fun checkDataProviderParameterIsClass() {
        if (methodParameter.hasAnnotation<ProvideBuilderData>()) {
            val builderDataProviderKType = methodParameter.type
            if (builderDataProviderKType.typeKind() != KTypeKind.KCLASS) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_DATA_PROVIDER_PARAMS_INVALID,
                )
            }
        }
    }

    fun checkDataProviderParameterValidClassType() {
        if (methodParameter.hasAnnotation<ProvideBuilderData>()) {
            val builderDataProviderKType = methodParameter.type

            try {
                KTypeUtil.classFromType(builderDataProviderKType)
            } catch (ex: IllegalStateException) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_DATA_PROVIDER_PARAMS_INVALID,
                    ex.message ?: "",
                )
            }
        }
    }

    fun checkParameterAllowedParameterAnnotations() {
        val hasAllowedMethodAnnotations =
            methodParameter.hasAnnotation<SetConceptIdentifierValue>() ||
                methodParameter.hasAnnotation<SetFacetValue>() ||
                methodParameter.hasAnnotation<ProvideBuilderData>()
        if (!isLastParameter) {
            if (!hasAllowedMethodAnnotations) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_MISSING_CONCEPT_IDENTIFIER_OR_SET_FACET_ANNOTATION,
                )
            }
        } else {
            if (!hasAllowedMethodAnnotations && !methodParameter.hasAnnotation<InjectBuilder>()) {
                throw BuilderMethodSyntaxException(
                    methodLocation = methodLocation.extendWithMethodParam(methodParameter),
                    errorCode =
                        BuilderErrorCode.BUILDER_PARAM_MISSING_CONCEPT_IDENTIFIER_OR_SET_FACET_ANNOTATION_OR_INJECTION,
                )
            }
        }
    }
}
