package org.codeblessing.sourceamazing.schema.typemirror

import java.lang.reflect.Method
import kotlin.reflect.KClass

interface MirrorFactoryApi {
    fun convertToMirrorHierarchy(clazz: KClass<*>): ClassMirrorInterface

    fun convertToMirrorHierarchy(method: Method): FunctionMirrorInterface
}