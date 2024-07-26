package org.codeblessing.sourceamazing.schema.typemirror

data class ParameterMirror(
    override val name: String?,
    override val type: TypeMirror,
    override val annotations: List<AnnotationMirror> = emptyList(),
): AbstractMirror(), ParameterMirrorInterface