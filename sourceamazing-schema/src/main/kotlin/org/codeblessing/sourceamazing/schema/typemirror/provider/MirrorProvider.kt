package org.codeblessing.sourceamazing.schema.typemirror.provider

fun interface MirrorProvider<out T> {
    fun provideMirror(): T
}