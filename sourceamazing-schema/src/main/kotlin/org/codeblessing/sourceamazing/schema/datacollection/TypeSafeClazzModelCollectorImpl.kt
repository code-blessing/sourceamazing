package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.datacollection.validation.ClazzModelValidator
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModel
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModelCollector
import org.codeblessing.sourceamazing.schema.typesafeapi.randomClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess

class TypeSafeClazzModelCollectorImpl(private val schemaAccess: TypeSafeSchemaAccess) : TypeSafeClazzModelCollector {

    private var sequenceNumber: Int = 0

    private val rootClazzModelId: ClazzModelId =
        ClazzModelId.ofRandom(clazz = schemaAccess.rootClazz(), suffix = "RootClazzModelId")

    private val clazzModels: MutableMap<ClazzModelId, TypeSafeClazzModel> = mutableMapOf()

    init {
        newClazzModel(schemaAccess.rootClazz(), rootClazzModelId)
    }

    override fun rootClazzModel(): TypeSafeClazzModel {
        return existingClazzModel(rootClazzModelId)
    }

    override fun existingClazzModel(clazzModelId: ClazzModelId): TypeSafeClazzModel {
        return clazzModels[clazzModelId] ?: throw IllegalArgumentException("No clazz model id '$clazzModelId' found.")
    }

    override fun newClazzModel(clazz: Clazz): TypeSafeClazzModel {
        return newClazzModel(clazz, clazz.randomClazzModelId())
    }

    override fun newClazzModel(clazz: Clazz, clazzModelId: ClazzModelId): TypeSafeClazzModel {
        val newClazzModelData = createNewClazzModel(clazz, clazzModelId)
        clazzModels[clazzModelId] = newClazzModelData
        return newClazzModelData
    }

    override fun validateAfterUpdate(clazzModel: TypeSafeClazzModel) {
        ClazzModelValidator.validateEntryWithoutReferenceAndCardinalityIntegrity(schemaAccess, clazzModel)
    }

    private fun createNewClazzModel(clazz: Clazz, clazzModelId: ClazzModelId): TypeSafeClazzModel {
        ClazzModelValidator.validateClazzForNewClazzModel(schemaAccess, clazz, clazzModelId, clazzModels.keys)
        return TypeSafeClazzModelImpl(sequenceNumber++, clazz, clazzModelId)
    }

    fun provideClazzModels(): List<TypeSafeClazzModel> {
        return clazzModels.values.toList()
    }
}
