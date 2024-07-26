package org.codeblessing.sourceamazing.schema.typemirror

interface ParameterMirrorInterface: AbstractMirrorInterface {
    val name: String?
    val type: TypeMirror
    override val annotations: List<AnnotationMirror>
}