package org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz

interface TypeSafeClazzPropertySchema {
    val enclosingClazz: Clazz
    val classProperty: ClassProperty
    val clazzPropertyClazz: Clazz
    val minimumOccurrences: Int
    val maximumOccurrences: Int

    fun isCompatibleClazzClass(clazz: KClass<*>): Boolean

    fun isCompatibleMaxCardinality(numberOfEntries: Int): Boolean

    fun isCompatibleMinCardinality(numberOfEntries: Int): Boolean

    fun isCompatibleClazzPropertyValue(value: Any): Boolean

    fun isCompatibleClazzPropertyReference(value: Any): Boolean
}
