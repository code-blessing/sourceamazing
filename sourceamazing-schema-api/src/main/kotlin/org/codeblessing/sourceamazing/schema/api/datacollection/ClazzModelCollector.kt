package org.codeblessing.sourceamazing.schema.api.datacollection

import kotlin.reflect.KClass

interface ClazzModelCollector {
    fun rootClazzModel(): ClazzModel

    fun newClazzModel(clazz: KClass<*>): ClazzModel

    fun newClazzModel(clazz: KClass<*>, clazzModelId: Any): ClazzModel

    fun existingClazzModel(clazzModelId: Any): ClazzModel
}
