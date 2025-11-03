package org.codeblessing.sourceamazing.schema.type

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance

object KTypeUtil {
    private val KOTLIN_PRIMITIVE_ARRAY_TYPES = setOf(BooleanArray::class, IntArray::class, LongArray::class, FloatArray::class, DoubleArray::class, CharArray::class, ShortArray::class)

    data class KTypeClassInformation(
        val clazz: KClass<*>,
        val isValueNullable: Boolean,
    )

    fun classInformationFromClass(kClass: KClass<*>, isNullable: Boolean): KTypeClassInformation {
        return KTypeClassInformation(kClass, isNullable)
    }

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

    fun kTypeFromProjection(projection: KTypeProjection, validVariances: Set<KVariance> = setOf(KVariance.INVARIANT, KVariance.OUT)): KType {
        requireNotNull(projection.variance) {
            "type can not have a star-type projection for function type."
        }
        require(validVariances.contains(projection.variance)) {
            "type can only have the variances $validVariances but was ${projection.variance} for function type."
        }
        val type = requireNotNull(projection.type) {
            "type must have a declared function type."
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

        if(classifier in KOTLIN_PRIMITIVE_ARRAY_TYPES) {
            return Array::class
        }
        return classifier
    }
}