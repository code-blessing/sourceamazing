package org.codeblessing.sourceamazing.schema.typemirror.provider

fun interface MirrorProvider<T> {
    fun provideMirror(): T
}