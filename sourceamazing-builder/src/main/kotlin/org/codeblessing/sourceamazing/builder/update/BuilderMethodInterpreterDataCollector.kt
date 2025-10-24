package org.codeblessing.sourceamazing.builder.update

import kotlin.reflect.KParameter
import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModel
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModelCollector

class BuilderMethodInterpreterDataCollector(
    private val clazzModelCollector: TypeSafeClazzModelCollector,
    val functionArguments: Map<KParameter, Any?>,
    val newClazzModelIdsFromSuperiorBuilder: Map<Alias, ClazzModelId>,
) {

    private val newClazzModelIds: MutableMap<Alias, ClazzModelId> = mutableMapOf()

    private fun newClazzModelIds(): Map<Alias, ClazzModelId> {
        return newClazzModelIds
    }

    fun newClazzModelIdsAndSuperiorClazzModelIds(): Map<Alias, ClazzModelId> {
        return newClazzModelIdsFromSuperiorBuilder + newClazzModelIds()
    }

    fun clazzModelIdByAlias(clazzAlias: Alias): ClazzModelId {
        return newClazzModelIds[clazzAlias]
            ?: newClazzModelIdsFromSuperiorBuilder[clazzAlias]
            ?: throw IllegalStateException("Can not find clazz id for alias '$clazzAlias'.")
    }

    fun newClazzData(alias: Alias, clazz: Clazz, clazzModelId: ClazzModelId) {
        newClazzModelIds[alias] = clazzModelId
        clazzModelCollector.newClazzModel(clazz, clazzModelId)
    }

    fun existingClazzData(clazzModelId: ClazzModelId): TypeSafeClazzModel {
        return clazzModelCollector.existingClazzModel(clazzModelId)
    }

    fun validateAfterUpdate(clazzModelData: TypeSafeClazzModel) {
        clazzModelCollector.validateAfterUpdate(clazzModelData)
    }

    fun getDataContext(): DataContext {
        return DataContext(functionArguments = functionArguments, newClazzModelIds = newClazzModelIds)
    }
}
