package org.codeblessing.sourceamazing.schema.typesafeapi

fun Any.toClazzModelId(): ClazzModelId {
    return ClazzModelId.of(this)
}
