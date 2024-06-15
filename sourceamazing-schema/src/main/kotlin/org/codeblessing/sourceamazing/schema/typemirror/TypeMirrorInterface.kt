package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

interface TypeMirrorInterface: MirrorProvider<TypeMirrorInterface> {
    val nullable: Boolean
}