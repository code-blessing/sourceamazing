package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.schema.type.KTypeUtil
import org.codeblessing.sourceamazing.schema.type.receiverParameter
import org.codeblessing.sourceamazing.schema.type.returnTypeOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.valueParameters

object SubBuilderHelper {

    fun getBuilderClassFromWithNewBuilderAnnotation(method: KFunction<*>): KClass<*>? {
        return method.findAnnotation<WithNewBuilder>()?.builderClass
    }

    fun getBuilderClassFromInjectBuilderParameter(method: KFunction<*>): KClass<*>? {
        val methodParameter = method.valueParameters.lastOrNull() ?: return null
        if(!methodParameter.hasAnnotation<InjectBuilder>()) {
            return null
        }

        val injectionBuilderKType = methodParameter.type
        val receiverParameterType = requireNotNull(injectionBuilderKType.receiverParameter()) {
            "receiverParameterType must not be null"
        }

        return KTypeUtil.classFromType(KTypeUtil.kTypeFromProjection(receiverParameterType))
    }

    fun getBuilderClassFromReturnType(method: KFunction<*>): KClass<*>? {
        val methodReturnType = method.returnTypeOrNull() ?: return null
        return KTypeUtil.classesInformationFromKType(methodReturnType).first().clazz
    }
}