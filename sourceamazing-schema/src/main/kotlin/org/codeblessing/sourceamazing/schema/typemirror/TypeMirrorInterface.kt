package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

interface TypeMirrorInterface {
    val signatureMirror: MirrorProvider<out SignatureMirror>
    val nullable: Boolean
}