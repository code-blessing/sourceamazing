package org.codeblessing.sourceamazing.schema.util

import org.codeblessing.sourceamazing.schema.type.enumValues
import org.codeblessing.sourceamazing.schema.type.isEnum
import kotlin.reflect.KClass

object EnumUtil {

    fun fromStringToEnum(enumStringValue: String, enumerationType: KClass<*>): Enum<*>? {
        return enumConstantList(enumerationType)
            .firstOrNull { enumConstant -> enumConstant.toString() == enumStringValue }
    }

    fun fromAnyToEnum(value: Any, enumerationType: KClass<*>): Enum<*>? {
        val enumValueAsString = when (value) {
            is String -> value
            is Enum<*> -> value.name
            else -> null
        } ?: return null

        return fromStringToEnum(enumValueAsString, enumerationType)
    }

    fun isEnumerationType(enumValue: Any, enumerationType: KClass<*>): Boolean {
        return enumValue.javaClass.isAssignableFrom(enumerationType.java)
    }

    fun enumConstantList(enumClass: KClass<*>): List<Enum<*>> {
        return enumClass.enumValues
    }

    fun isSameOrSubsetEnumerationClass(fullEnumClass: KClass<*>, fullOrSubsetEnumClass: KClass<*>): Boolean {
        if(fullEnumClass == fullOrSubsetEnumClass) {
            return true
        }
        if(!fullEnumClass.isEnum || !fullOrSubsetEnumClass.isEnum) {
            return false
        }
        return enumConstantList(fullOrSubsetEnumClass)
            .all { subsetEnumValue: Enum<*> -> fromStringToEnum(subsetEnumValue.name, fullEnumClass) != null }
    }

}