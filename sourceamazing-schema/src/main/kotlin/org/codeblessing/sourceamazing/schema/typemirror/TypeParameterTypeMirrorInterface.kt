package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider


interface TypeParameterTypeMirrorInterface: MirrorProvider<TypeParameterTypeMirrorInterface>, SignatureMirror, AbstractMirrorInterface {
    override val annotations: List<AnnotationMirror>
}