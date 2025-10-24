package org.codeblessing.sourceamazing.builder.factories

import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModel
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModelCollector

class NoopTypeSafeClazzModelCollector : TypeSafeClazzModelCollector {
    override fun rootClazzModel(): TypeSafeClazzModel {
        throw UnsupportedOperationException()
    }

    override fun newClazzModel(clazz: Clazz): TypeSafeClazzModel {
        throw UnsupportedOperationException()
    }

    override fun newClazzModel(clazz: Clazz, clazzModelId: ClazzModelId): TypeSafeClazzModel {
        throw UnsupportedOperationException()
    }

    override fun existingClazzModel(clazzModelId: ClazzModelId): TypeSafeClazzModel {
        throw UnsupportedOperationException()
    }

    override fun validateAfterUpdate(clazzModel: TypeSafeClazzModel) {
        throw UnsupportedOperationException()
    }
}
