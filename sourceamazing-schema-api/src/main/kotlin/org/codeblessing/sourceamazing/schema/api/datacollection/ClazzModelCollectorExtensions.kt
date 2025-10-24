package org.codeblessing.sourceamazing.schema.api.datacollection

inline fun <reified C> ClazzModelCollector.newClazzModel(clazzModelId: Any): ClazzModel =
    newClazzModel(C::class, clazzModelId)

inline fun <reified C> ClazzModelCollector.newClazzModel(): ClazzModel = newClazzModel(C::class)
