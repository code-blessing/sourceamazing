package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModel

class TypeSafeClazzModelImpl(
    override val sequenceNumber: Int,
    override val clazz: Clazz,
    override val clazzModelId: ClazzModelId,
) : TypeSafeClazzModel {
    private val mutableClazzProperties: MutableMap<ClassProperty, MutableList<Any>> = mutableMapOf()

    override fun hasClazzProperty(classProperty: ClassProperty): Boolean {
        return mutableClazzProperties.containsKey(classProperty)
    }

    override fun getClazzPropertyValues(classProperty: ClassProperty): List<Any> {
        return mutableClazzProperties[classProperty] ?: emptyList()
    }

    override fun getClassPropertyNames(): Set<ClassProperty> {
        return mutableClazzProperties.keys
    }

    override fun allClazzProperties(): Map<ClassProperty, List<Any>> {
        return mutableClazzProperties.toMap()
    }

    override fun replaceWithValues(classProperty: ClassProperty, values: List<Any>): TypeSafeClazzModelImpl {
        mutableClazzProperties[classProperty] = values.map { transformSingleValue(it) }.toMutableList()
        return this
    }

    override fun addValue(classProperty: ClassProperty, value: Any): TypeSafeClazzModelImpl {
        assureClazzPropertyValueList(classProperty).add(transformSingleValue(value))
        return this
    }

    override fun addValues(classProperty: ClassProperty, values: List<Any>): TypeSafeClazzModel {
        assureClazzPropertyValueList(classProperty).addAll(values.map { transformSingleValue(it) })
        return this
    }

    override fun replaceWithClazzReferences(
        classProperty: ClassProperty,
        references: List<Any>,
    ): TypeSafeClazzModelImpl {
        mutableClazzProperties[classProperty] = references.map { transformSingleClazzReference(it) }.toMutableList()
        return this
    }

    override fun addClazzReference(classProperty: ClassProperty, reference: Any): TypeSafeClazzModelImpl {
        assureClazzPropertyValueList(classProperty).add(transformSingleClazzReference(reference))
        return this
    }

    override fun addClazzReferences(classProperty: ClassProperty, references: List<Any>): TypeSafeClazzModel {
        assureClazzPropertyValueList(classProperty).addAll(references.map { transformSingleClazzReference(it) })
        return this
    }

    override fun describe(): String {
        val clazzPropertyDescription =
            mutableClazzProperties.map { (key, value) -> describeClazzProperty(key, value) }.joinToString("\n")

        return "${clazz.simpleName()}:${clazzModelId.name} {\n$clazzPropertyDescription\n}"
    }

    private fun describeClazzProperty(key: ClassProperty, value: MutableList<Any>): String {
        return "  ${key.simpleName()}:[ ${value.joinToString(", ") { "'${it}'" }} ]"
    }

    private fun transformSingleValue(value: Any): Any {
        return value
    }

    private fun transformSingleClazzReference(clazzReference: Any): Any {
        if (clazzReference is TypeSafeClazzModel) {
            // allow to pass a TypeSafeClazzModelAdapter instance to reference this instance
            return clazzReference.clazzModelId
        }
        if (clazzReference !is ClazzModelId) {
            return ClazzModelId.of(clazzReference)
        }
        return clazzReference
    }

    private fun assureClazzPropertyValueList(classProperty: ClassProperty): MutableList<Any> {
        val currentList = mutableClazzProperties[classProperty]
        return currentList ?: createEmptyListForClazzProperty(classProperty)
    }

    private fun createEmptyListForClazzProperty(classProperty: ClassProperty): MutableList<Any> {
        val list = mutableListOf<Any>()
        mutableClazzProperties[classProperty] = list
        return list
    }
}
