package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodParameterSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.type.KTypeUtil
import org.codeblessing.sourceamazing.schema.type.receiverParameter
import org.codeblessing.sourceamazing.schema.type.returnTypeOrNull
import org.codeblessing.sourceamazing.schema.type.valueParameters
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.valueParameters

object BuilderClassHelper {

    fun getSubBuilderClass(method: KFunction<*>): KClass<*>? {
        val subBuilderClassFromNewBuilderAnnotation = getBuilderClassFromWithNewBuilderAnnotation(method)
        val subBuilderClassFromReturnType = getBuilderClassFromReturnType(method)
        val subBuilderClassFromInjectBuilderAnnotation = getBuilderClassFromInjectBuilderParameter(method)

        if(subBuilderClassFromReturnType == null
            && subBuilderClassFromInjectBuilderAnnotation == null
            && subBuilderClassFromNewBuilderAnnotation == null) {
            return null
        }

        if(subBuilderClassFromReturnType != null && subBuilderClassFromInjectBuilderAnnotation != null) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.BUILDER_INJECTION_AND_RETURN_AT_SAME_TIME)
        }

        if(subBuilderClassFromReturnType == null && subBuilderClassFromInjectBuilderAnnotation == null) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.BUILDER_IN_WITH_NEW_BUILDER_MUST_BE_USED)
        }

        val subBuilderClass = subBuilderClassFromReturnType ?: subBuilderClassFromInjectBuilderAnnotation


        if(subBuilderClassFromNewBuilderAnnotation != null) {
            if (subBuilderClassFromNewBuilderAnnotation != subBuilderClass) {
                throw BuilderMethodSyntaxException(method, BuilderErrorCode.BUILDER_IN_WITH_NEW_BUILDER_MUST_BE_SAME)
            }
        }

        return subBuilderClass
    }

    fun getBuilderClassFromInjectBuilderParameter(method: KFunction<*>): KClass<*>? {
        if(method.valueParameters.isEmpty()) {
            return null
        }

        if(method.valueParameters.size > 1) {
            val lastParameterIndex = method.valueParameters.size - 1
            method.valueParameters
                .filterIndexed { index, _ -> index < lastParameterIndex }
                .forEach { methodParameter ->
                    if(methodParameter.hasAnnotation<InjectBuilder>()) {
                        throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_ONLY_LAST_PARAM_CAN_BE_INJECTION)
                    }
                }
        }

        val methodParameter = method.valueParameters.last()
        if(!methodParameter.hasAnnotation<InjectBuilder>()) {
            return null
        }

        val injectionBuilderKType = methodParameter.type
        if(injectionBuilderKType.isMarkedNullable) {
            throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_BE_NULLABLE)
        }

        if(injectionBuilderKType.returnTypeOrNull() != null) {
            throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_HAVE_RETURN_TYPE)
        }

        val receiverParameterType = injectionBuilderKType.receiverParameter()

        if(receiverParameterType == null || injectionBuilderKType.valueParameters().isNotEmpty()) {
            throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_INJECTION_PARAMS_INVALID)
        }

        val receiverParameterKType = try {
            KTypeUtil.kTypeFromProjection(receiverParameterType)
        } catch (ex: IllegalStateException) {
            throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_INJECTION_INVALID_RECEIVER_PARAM, ex.message ?: "")
        }
        if(receiverParameterKType.isMarkedNullable) {
            throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_INJECTION_NOT_NULLABLE_RECEIVER_PARAM)
        }

        return try {
            KTypeUtil.classFromType(receiverParameterKType)
        } catch (ex: IllegalStateException) {
            throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_INJECTION_INVALID_RECEIVER_PARAM, ex.message ?: "")
        }
    }

    private fun getBuilderClassFromWithNewBuilderAnnotation(method: KFunction<*>): KClass<*>? {
        return method.findAnnotation<WithNewBuilder>()?.builderClass
    }

    fun getBuilderClassFromReturnType(method: KFunction<*>): KClass<*>? {
        if(method.returnTypeOrNull() == null) {
            return null
        }
        val classesInformationFromKType = try {
            KTypeUtil.classesInformationFromKType(method.returnType)
        } catch (ex: IllegalStateException) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.BUILDER_MUST_RETURN_BUILDER_CLASS, ex.message ?:"")
        }
        if(classesInformationFromKType.isEmpty()) {
            return null
        }

        if(classesInformationFromKType.size > 1) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.BUILDER_MUST_RETURN_BUILDER_CLASS, "")
        }

        val classInformation = classesInformationFromKType.first()
        if(classInformation.isValueNullable) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.BUILDER_RETURNED_CAN_NOT_BE_NULLABLE)
        }

        return classInformation.clazz
    }
}