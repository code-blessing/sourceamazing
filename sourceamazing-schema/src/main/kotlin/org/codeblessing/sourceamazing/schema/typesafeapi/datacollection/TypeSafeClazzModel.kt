package org.codeblessing.sourceamazing.schema.typesafeapi.datacollection

import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId

interface TypeSafeClazzModel {
    val sequenceNumber: Int
    val clazz: Clazz
    val clazzModelId: ClazzModelId

    fun describe(): String

    fun allClazzProperties(): Map<ClassProperty, List<Any>>

    fun hasClazzProperty(classProperty: ClassProperty): Boolean

    fun getClazzPropertyValues(classProperty: ClassProperty): List<Any>

    fun getClassPropertyNames(): Set<ClassProperty>

    fun replaceWithValues(classProperty: ClassProperty, values: List<Any>): TypeSafeClazzModel

    fun addValue(classProperty: ClassProperty, value: Any): TypeSafeClazzModel

    fun addValues(classProperty: ClassProperty, values: List<Any>): TypeSafeClazzModel

    fun replaceWithClazzReferences(classProperty: ClassProperty, references: List<Any>): TypeSafeClazzModel

    fun addClazzReference(classProperty: ClassProperty, reference: Any): TypeSafeClazzModel

    fun addClazzReferences(classProperty: ClassProperty, references: List<Any>): TypeSafeClazzModel
}
