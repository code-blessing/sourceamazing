package org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.typesafeadapter

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.datacollection.ClazzModel
import org.codeblessing.sourceamazing.schema.api.datacollection.ClazzModelCollector
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModelCollector
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazz
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazzModelId

data class TypeSafeClazzModelCollectorAdapter(private val typeSafeClazzModelCollector: TypeSafeClazzModelCollector) :
    ClazzModelCollector {
    override fun rootClazzModel(): ClazzModel {
        return TypeSafeClazzModelAdapter(typeSafeClazzModelCollector.rootClazzModel())
    }

    override fun newClazzModel(clazz: KClass<*>): ClazzModel {
        return TypeSafeClazzModelAdapter(typeSafeClazzModelCollector.newClazzModel(clazz.toClazz()))
    }

    override fun newClazzModel(clazz: KClass<*>, clazzModelId: Any): ClazzModel {
        return TypeSafeClazzModelAdapter(
            typeSafeClazzModelCollector.newClazzModel(clazz.toClazz(), clazzModelId.toClazzModelId())
        )
    }

    override fun existingClazzModel(clazzModelId: Any): ClazzModel {
        return TypeSafeClazzModelAdapter(typeSafeClazzModelCollector.existingClazzModel(clazzModelId.toClazzModelId()))
    }
}
