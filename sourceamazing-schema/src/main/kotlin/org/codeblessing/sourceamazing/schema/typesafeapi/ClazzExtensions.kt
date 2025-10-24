package org.codeblessing.sourceamazing.schema.typesafeapi

import kotlin.reflect.KClass

fun KClass<*>.toClazz(): Clazz {
    return Clazz.of(this)
}

fun Clazz.randomClazzModelId(): ClazzModelId {
    return ClazzModelId.ofRandom(this)
}
