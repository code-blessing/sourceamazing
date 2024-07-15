package org.codeblessing.sourceamazing.schema.typemirror.provider

import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror

object ClassMirrorProviderHelper {

    fun Collection<ClassMirrorProvider>.provideClassMirrors(): List<ClassMirror> {
        return this.map { it.provideClassMirror() }
    }

}