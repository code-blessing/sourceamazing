package org.codeblessing.sourceamazing.schema.type

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance

object KTypeUtil {

    data class KTypeClassInformation(
        val clazz: KClass<*>,
        val isValueNullable: Boolean,
    )

    fun classesInformationFromKType(kType: KType): List<KTypeClassInformation> {
        val classInfos: MutableList<KTypeClassInformation> = mutableListOf()
        val classifier = classFromType(kType)
        classInfos.add(KTypeClassInformation(classifier, kType.isMarkedNullable))
        kType.arguments.forEach { kTypeProjection ->
            val argumentKType = kTypeFromProjection(kTypeProjection)
            classInfos.add(KTypeClassInformation(classFromType(argumentKType), argumentKType.isMarkedNullable))
        }
        return classInfos
    }

    private fun kTypeFromProjection(projection: KTypeProjection): KType {
        requireNotNull(projection.variance) {
            "type can not have a star-type projection for function return type."
        }
        require(projection.variance != KVariance.IN) {
            "type can not have an In-variant projection for function return type."
        }
        val type = requireNotNull(projection.type) {
            "type must have a declared function return type."
        }

        return type
    }

    fun classFromType(type: KType): KClass<*> {
        val classifier = type.classifier
        requireNotNull(classifier) {
            "type classifier was null."
        }
        require(classifier is KClass<*>) {
            "type classifier is not a class but was $classifier."
        }
        return classifier
    }
}