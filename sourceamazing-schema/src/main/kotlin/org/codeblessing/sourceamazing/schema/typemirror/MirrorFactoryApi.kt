package org.codeblessing.sourceamazing.schema.typemirror

import java.lang.reflect.Method
import kotlin.reflect.KClass

interface MirrorFactoryApi {
    fun convertToMirrorHierarchy(clazz: KClass<*>): ClassMirror

    fun convertToMirrorHierarchy(method: Method): MethodMirror
}