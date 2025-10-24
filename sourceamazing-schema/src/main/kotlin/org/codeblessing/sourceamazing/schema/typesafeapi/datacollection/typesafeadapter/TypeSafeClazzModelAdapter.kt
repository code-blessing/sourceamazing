package org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.typesafeadapter

import org.codeblessing.sourceamazing.schema.api.ClazzAndModelId
import org.codeblessing.sourceamazing.schema.api.datacollection.ClazzModel
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.TypeSafeClazzAndModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModel
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazzProperty

data class TypeSafeClazzModelAdapter(private val typeSafeClazzModel: TypeSafeClazzModel) : ClazzModel {

    override val clazzAndModelId: ClazzAndModelId =
        ClazzAndModelId(typeSafeClazzModel.clazz.clazz, typeSafeClazzModel.clazzModelId.name)

    override fun replaceWithClazzPropertyValues(clazzProperty: String, values: List<Any>): ClazzModel {
        typeSafeClazzModel.replaceWithValues(
            clazzProperty.toClazzProperty(),
            values.map { adaptIngoingClazzPropertyValue(it) },
        )
        return this
    }

    override fun replaceWithClazzPropertyReferences(clazzProperty: String, references: List<Any>): ClazzModel {
        typeSafeClazzModel.replaceWithValues(
            clazzProperty.toClazzProperty(),
            references.map { adaptIngoingClazzPropertyReference(it) },
        )
        return this
    }

    override fun addClazzPropertyValue(clazzProperty: String, value: Any): ClazzModel {
        typeSafeClazzModel.addValue(clazzProperty.toClazzProperty(), adaptIngoingClazzPropertyValue(value))
        return this
    }

    override fun addClazzPropertyReference(clazzProperty: String, references: Any): ClazzModel {
        typeSafeClazzModel.addValue(clazzProperty.toClazzProperty(), adaptIngoingClazzPropertyReference(references))
        return this
    }

    override fun addClazzPropertyValues(clazzProperty: String, values: List<Any>): ClazzModel {
        typeSafeClazzModel.addValues(clazzProperty.toClazzProperty(), values.map { adaptIngoingClazzPropertyValue(it) })
        return this
    }

    override fun addClazzPropertyReferences(clazzProperty: String, references: List<Any>): ClazzModel {
        typeSafeClazzModel.addValues(
            clazzProperty.toClazzProperty(),
            references.map { adaptIngoingClazzPropertyReference(it) },
        )
        return this
    }

    private fun adaptIngoingClazzPropertyValue(clazzPropertyValue: Any): Any {
        return clazzPropertyValue
    }

    private fun adaptIngoingClazzPropertyReference(clazzPropertyReference: Any): Any {
        if (clazzPropertyReference is TypeSafeClazzModelAdapter) {
            return clazzPropertyReference.typeSafeClazzModel.clazzModelId
        }
        if (clazzPropertyReference is TypeSafeClazzAndModelId) {
            return clazzPropertyReference.clazzModelId
        }
        return ClazzModelId.of(clazzPropertyReference)
    }

    private fun adaptOutgoingClazzPropertyValue(clazzPropertyValue: Any): Any {
        if (clazzPropertyValue is TypeSafeClazzModel) {
            return TypeSafeClazzModelAdapter(clazzPropertyValue)
        }
        if (clazzPropertyValue is ClazzModelId) {
            return clazzPropertyValue.name
        }
        return clazzPropertyValue
    }
}
