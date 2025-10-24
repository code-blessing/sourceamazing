package org.codeblessing.sourceamazing.schema.typesafeapi.datacollection

import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId

interface TypeSafeClazzModelCollector {
    fun rootClazzModel(): TypeSafeClazzModel

    fun newClazzModel(clazz: Clazz): TypeSafeClazzModel

    fun newClazzModel(clazz: Clazz, clazzModelId: ClazzModelId): TypeSafeClazzModel

    fun existingClazzModel(clazzModelId: ClazzModelId): TypeSafeClazzModel

    fun validateAfterUpdate(clazzModel: TypeSafeClazzModel)
}
