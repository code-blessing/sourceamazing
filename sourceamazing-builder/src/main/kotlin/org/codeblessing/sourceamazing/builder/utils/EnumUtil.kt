package org.codeblessing.sourceamazing.builder.utils

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.utils.type.enumValues
import org.codeblessing.sourceamazing.schema.utils.type.isEnum

object EnumUtil {

    fun fromStringToEnum(enumStringValue: String, enumerationType: KClass<*>): Enum<*>? {
        return enumerationType.enumValues.firstOrNull { enumConstant -> enumConstant.toString() == enumStringValue }
    }

    fun isEnumerationType(enumValue: Any, enumerationType: KClass<*>): Boolean {
        return enumValue.javaClass.isAssignableFrom(enumerationType.java)
    }

    fun isSameOrSubsetEnumerationClass(fullEnumClass: KClass<*>, fullOrSubsetEnumClass: KClass<*>): Boolean {
        if (fullEnumClass == fullOrSubsetEnumClass) {
            return true
        }
        if (!fullEnumClass.isEnum || !fullOrSubsetEnumClass.isEnum) {
            return false
        }
        return fullOrSubsetEnumClass.enumValues.all { subsetEnumValue: Enum<*> ->
            fromStringToEnum(subsetEnumValue.name, fullEnumClass) != null
        }
    }
}
