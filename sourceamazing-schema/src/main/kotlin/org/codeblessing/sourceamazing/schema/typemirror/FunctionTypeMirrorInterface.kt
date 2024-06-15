package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

interface FunctionTypeMirrorInterface: TypeMirrorInterface  {
    val functionMirror: MirrorProvider<FunctionMirrorInterface>
    // here is room for concrete type arguments
}