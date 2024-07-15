package org.codeblessing.sourceamazing.schema.typemirror

data class ClassQualifierMirror(
    val className: String,
    val packageName: String = "",
)