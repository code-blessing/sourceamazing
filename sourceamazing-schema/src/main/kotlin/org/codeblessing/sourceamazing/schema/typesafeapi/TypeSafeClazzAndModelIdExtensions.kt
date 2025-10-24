package org.codeblessing.sourceamazing.schema.typesafeapi

import org.codeblessing.sourceamazing.schema.api.ClazzAndModelId

fun ClazzAndModelId.toTypeSafeClazzAndModelId(): TypeSafeClazzAndModelId {
    return TypeSafeClazzAndModelId(clazz.toClazz(), clazzModelId.toClazzModelId())
}
