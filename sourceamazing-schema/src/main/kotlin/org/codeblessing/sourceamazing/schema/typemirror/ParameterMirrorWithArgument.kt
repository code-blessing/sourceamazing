package org.codeblessing.sourceamazing.schema.typemirror

data class ParameterMirrorWithArgument(
    val index: Int,
    val param: ParameterMirror,
    val value: Any?
)