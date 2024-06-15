package org.codeblessing.sourceamazing.schema.typemirror

data class ParameterMirrorWithArgument(
    val index: Int,
    val param: ParameterMirrorInterface,
    val value: Any?
)