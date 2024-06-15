package org.codeblessing.sourceamazing.schema.type

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

fun KType.receiverParameter() = if(isExtensionFunction()) this.arguments.firstOrNull() else null

fun KType.isExtensionFunction(): Boolean = annotations.any { it.annotationClass == ExtensionFunctionType::class }

fun KType.valueParameters() = arguments
    .subList(if(isExtensionFunction()) 1 else 0, arguments.size - 1)

fun KType.returnTypeOrNull(): KType? {
    val lastArgument = arguments.lastOrNull() ?: return null
    val lastArgumentType = lastArgument.type ?: return null
    if(lastArgumentType.isUnitType()) {
        return null
    }
    return lastArgumentType
}

fun KType.isUnitType(): Boolean {
    return this.jvmErasure == Unit::class
}

enum class KTypeKind {
    OTHER_TYPE,
    TYPE_PARAMETER,
    FUNCTION,
    KCLASS,
}

fun KType.typeKind(): KTypeKind {
    val classifier = classifier ?: return KTypeKind.OTHER_TYPE
    val hasTypeParameter = arguments.any { argument: KTypeProjection ->
        return@any argument.type?.classifier is KTypeParameter
    }
    if(hasTypeParameter) {
        return KTypeKind.TYPE_PARAMETER
    }
    // see https://kt.academy/article/ak-reflection-type
    return when (classifier) {
        is KFunction<*> -> {
            return KTypeKind.FUNCTION
        }
        is KClass<*> -> {
            return if(classifier.isSubclassOf(Function::class)) {
                KTypeKind.FUNCTION
            } else {
                KTypeKind.KCLASS
            }
        }
        else -> KTypeKind.OTHER_TYPE
    }
}