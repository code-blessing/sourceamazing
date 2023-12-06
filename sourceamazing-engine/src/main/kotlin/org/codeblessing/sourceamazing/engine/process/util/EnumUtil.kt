package org.codeblessing.sourceamazing.engine.process.util

import kotlin.reflect.KClass

object EnumUtil {

    fun fromStringToEnum(enumStringValue: String, enumerationType: KClass<*>): Any? {
        return enumerationType.java.enumConstants
            .firstOrNull { enumConstant -> enumConstant.toString() == enumStringValue }
    }

    fun isEnumerationType(enumFacetValue: Any, enumerationType: KClass<*>): Boolean {
        return enumFacetValue.javaClass.isAssignableFrom(enumerationType.java)
    }

}