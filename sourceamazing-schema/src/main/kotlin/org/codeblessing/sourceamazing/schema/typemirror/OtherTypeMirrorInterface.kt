package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider


interface OtherTypeMirrorInterface: MirrorProvider<OtherTypeMirrorInterface>, SignatureMirror, AbstractMirrorInterface {
    override val annotations: List<AnnotationMirror>
}