package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

data class FakeTypeMirror(
    override val signatureMirror: MirrorProvider<out SignatureMirror>,
    override val nullable: Boolean = false,
): TypeMirrorInterface {
    fun nullable(isNullable: Boolean): FakeTypeMirror {
        return copy(
            nullable = isNullable
        )
    }
}