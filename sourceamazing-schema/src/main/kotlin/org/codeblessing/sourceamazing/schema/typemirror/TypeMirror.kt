package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

data class TypeMirror(
    val signatureMirror: MirrorProvider<out SignatureMirror>,
    val nullable: Boolean = false,
) {
    fun nullable(isNullable: Boolean): TypeMirror {
        return copy(
            nullable = isNullable
        )
    }
}