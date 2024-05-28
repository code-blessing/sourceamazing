package org.codeblessing.sourceamazing.schema.documentation

import kotlin.reflect.KClass

object TypesAsTextFunctions {

    fun KClass<out Annotation>.annotationText(): String {
        return "@${fromClazzToShortText(this.java)}"
    }

    fun KClass<*>.shortText(): String {
        return fromClazzToShortText(this.java)
    }

    fun KClass<*>.longText(): String {
        return fromClazzToLongText(java)
    }

    fun Class<*>.shortText(): String {
        return fromClazzToShortText(this)
    }

    fun Class<*>.longText(): String {
        return fromClazzToLongText(this)
    }

    private fun fromClazzToShortText(clazz: Class<*>): String {
        return clazz.simpleName
    }

    private fun fromClazzToLongText(clazz: Class<*>): String {
        return clazz.name
    }

}