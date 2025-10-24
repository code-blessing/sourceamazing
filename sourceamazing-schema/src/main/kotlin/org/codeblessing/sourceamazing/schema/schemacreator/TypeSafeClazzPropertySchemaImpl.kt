package org.codeblessing.sourceamazing.schema.schemacreator

import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf
import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzPropertySchema
import org.codeblessing.sourceamazing.schema.utils.type.isEnum

data class TypeSafeClazzPropertySchemaImpl(
    override val enclosingClazz: Clazz,
    override val classProperty: ClassProperty,
    override val clazzPropertyClazz: Clazz,
    override val minimumOccurrences: Int,
    override val maximumOccurrences: Int,
) : TypeSafeClazzPropertySchema {
    override fun isCompatibleClazzClass(clazz: KClass<*>): Boolean {
        return isCompatibleClazzClass(clazzPropertyClazz, clazz)
    }

    override fun isCompatibleClazzPropertyValue(value: Any): Boolean {
        if (clazzPropertyClazz.clazz.isEnum) {
            if (value is Enum<*> && value::class == clazzPropertyClazz.clazz) {
                return true
            }
        }

        return isCompatibleClazzClass(value::class)
    }

    override fun isCompatibleClazzPropertyReference(value: Any): Boolean {
        return true
    }

    override fun isCompatibleMaxCardinality(numberOfEntries: Int): Boolean {
        return numberOfEntries <= maximumOccurrences
    }

    override fun isCompatibleMinCardinality(numberOfEntries: Int): Boolean {
        return numberOfEntries >= minimumOccurrences
    }

    private fun isCompatibleClazzClass(baseClazz: Clazz, clazz: KClass<*>): Boolean {
        return baseClazz.clazz == clazz || baseClazz.clazz.isSuperclassOf(clazz)
    }
}
