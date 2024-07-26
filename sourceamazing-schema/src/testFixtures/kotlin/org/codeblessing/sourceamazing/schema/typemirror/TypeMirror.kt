package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

data class TypeMirror(
    override val signatureMirror: MirrorProvider<out SignatureMirror>,
    override val nullable: Boolean = false,
): TypeMirrorInterface {
    fun nullable(isNullable: Boolean): TypeMirror {
        return copy(
            nullable = isNullable
        )
    }
}