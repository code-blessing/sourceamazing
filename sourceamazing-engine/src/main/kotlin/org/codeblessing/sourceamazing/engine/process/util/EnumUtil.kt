package org.codeblessing.sourceamazing.engine.process.util

import kotlin.reflect.KClass

object EnumUtil {

    fun fromStringToEnum(enumStringValue: String, enumerationType: KClass<*>): Any? {
        return enumConstants(enumerationType)
            .firstOrNull { enumConstant -> enumConstant.toString() == enumStringValue }
    }

    fun isEnumerationType(enumFacetValue: Any, enumerationType: KClass<*>): Boolean {
        return enumFacetValue.javaClass.isAssignableFrom(enumerationType.java)
    }

    fun enumConstantStringList(facetEnumType: KClass<*>): List<String> {
        return enumConstants(facetEnumType).map { it.toString() }
    }

    private fun enumConstants(facetEnumType: KClass<*>): Array<out Any> {
        return facetEnumType.java.enumConstants
    }

}