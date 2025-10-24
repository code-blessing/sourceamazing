package org.codeblessing.sourceamazing.schema.utils

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

fun KClass<*>.shortName(): String {
    return simpleName ?: longText()
}

private fun KClass<*>.longText(): String {
    return java.name
}

fun KProperty<*>.shortName(): String {
    return this.name
}

fun KParameter.shortName(): String {
    return this.name ?: this.toString()
}

fun KAnnotatedElement.shortName(): String {
    return when (this) {
        is KClass<*> -> this.shortName()
        is KProperty<*> -> this.shortName()
        is KParameter -> this.shortName()
        else -> this.toString()
    }
}
