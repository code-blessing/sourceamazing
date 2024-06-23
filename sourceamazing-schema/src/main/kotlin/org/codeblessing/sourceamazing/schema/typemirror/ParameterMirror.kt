package org.codeblessing.sourceamazing.schema.typemirror

data class ParameterMirror(
    val name: String?,
    val annotations: List<AnnotationMirror>,
    val type: TypeMirror,
)