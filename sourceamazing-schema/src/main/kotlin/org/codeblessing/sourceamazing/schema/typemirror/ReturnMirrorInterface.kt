package org.codeblessing.sourceamazing.schema.typemirror

interface ReturnMirrorInterface: AbstractMirrorInterface {
    val type: TypeMirror
    override val annotations: List<AnnotationMirror>
}