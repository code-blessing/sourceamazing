package org.codeblessing.sourceamazing.schema.typemirror.provider

import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror

fun interface ClassMirrorProvider {
    fun provideClassMirror(): ClassMirror
}