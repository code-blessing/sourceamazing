package org.codeblessing.sourceamazing.schema.typemirror

import kotlin.reflect.KClass

interface MirrorFactoryApi {
    fun convertToMirrorHierarchy(clazz: KClass<*>): ClassMirror
}