package org.codeblessing.sourceamazing.schema.typemirror

data class MethodMirror(
    val methodName: String,
    val annotations: List<AnnotationMirror>,
    val returnType: TypeMirror,
    val parameters: List<ParameterMirror>,
)