package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
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
            throw BuilderMethodSyntaxException(
                method, "A builder method can not have an injected builder (with " +
                        "annotation ${InjectBuilder::class.annotationText()}) " +
                        "and at the same time a builder as return type."
            )
        }

        if(subBuilderClassFromReturnType == null && subBuilderClassFromInjectBuilderAnnotation == null) {
            throw BuilderMethodSyntaxException(
                method, "The builder class declared within the annotation " +
                        "${WithNewBuilder::class.annotationText()} must be the " +
                        "same as the return type or the injection type."
            )
        }

        val subBuilderClass = subBuilderClassFromReturnType ?: subBuilderClassFromInjectBuilderAnnotation


        if(subBuilderClassFromNewBuilderAnnotation != null) {
            if (subBuilderClassFromNewBuilderAnnotation != subBuilderClass) {
                throw BuilderMethodSyntaxException(
                    method, "The builder class declared within the annotation " +
                            "${WithNewBuilder::class.annotationText()} must be the " +
                            "same as the return type or the injected builder type " +
                            "of the method."
                )

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
                        throw BuilderMethodSyntaxException(
                            method, "Only the last parameter of the method " +
                                    "can have the annotation ${InjectBuilder::class.annotationText()}."
                        )
                    }
                }
        }

        val methodParameter = method.valueParameters.last()
        if(!methodParameter.hasAnnotation<InjectBuilder>()) {
            return null
        }

        val injectionBuilderKType = methodParameter.type
        if(injectionBuilderKType.returnTypeOrNull() != null) {
            throw BuilderMethodSyntaxException(
                method, "An injected builder " +
                        "(parameter with ${InjectBuilder::class.annotationText()}) " +
                        "can not have a return type."
            )
        }

        val receiverParameterType = injectionBuilderKType.receiverParameter()

        if(receiverParameterType == null || injectionBuilderKType.valueParameters().isNotEmpty()) {
            throw BuilderMethodSyntaxException(
                method, "An injected builder " +
                        "(parameter with ${InjectBuilder::class.annotationText()}) " +
                        "must have as sole parameter a receiver parameter (extension function) type." +
                        "Its declaration must be \'<Builder>.() -> Unit\'."
            )
        }

        val receiverParameterKType = try {
            KTypeUtil.kTypeFromProjection(receiverParameterType)
        } catch (ex: IllegalStateException) {
            throw BuilderMethodSyntaxException(
                method, "The receiver type of the injected builder is invalid." +
                        "${ex.message}"
            )
        }
        if(receiverParameterKType.isMarkedNullable) {
            throw BuilderMethodSyntaxException(
                method, "The receiver type of the injected builder can not be nullable."
            )
        }

        return try {
            KTypeUtil.classFromType(receiverParameterKType)
        } catch (ex: IllegalStateException) {
            throw BuilderMethodSyntaxException(
                method, "The receiver type of the injected builder is invalid." +
                        "${ex.message}"
            )
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
            throw BuilderMethodSyntaxException(method, "The return type of a builder method can only return a builder class. ${ex.message}")
        }
        if(classesInformationFromKType.isEmpty()) {
            return null
        }

        if(classesInformationFromKType.size > 1) {
            throw BuilderMethodSyntaxException(method, "The return type of a builder method can only return a builder class.")
        }

        val classInformation = classesInformationFromKType.first()
        if(classInformation.isValueNullable) {
            throw BuilderMethodSyntaxException(method, "The return type of a builder method can not be nullable.")
        }

        return classInformation.clazz
    }

}