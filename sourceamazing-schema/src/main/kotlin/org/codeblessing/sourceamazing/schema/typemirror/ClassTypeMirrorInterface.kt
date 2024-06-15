package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

interface ClassTypeMirrorInterface: TypeMirrorInterface {
    val classMirror: MirrorProvider<ClassMirrorInterface>
    val genericTypeArguments: List<MirrorProvider<TypeMirrorInterface>>
}