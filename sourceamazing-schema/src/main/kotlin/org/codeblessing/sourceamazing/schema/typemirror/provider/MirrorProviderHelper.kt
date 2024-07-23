package org.codeblessing.sourceamazing.schema.typemirror.provider


object MirrorProviderHelper {

    fun <M> Collection<MirrorProvider<M>>.provideClassMirrors(): List<M> {
        return this.map { it.provideMirror() }
    }

}