package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import kotlin.reflect.KClass

fun interface ClassMirrorCreatorCallable {
    fun classMirrorCreator(clazz: KClass<*>): MirrorProvider<ClassMirrorInterface>
}