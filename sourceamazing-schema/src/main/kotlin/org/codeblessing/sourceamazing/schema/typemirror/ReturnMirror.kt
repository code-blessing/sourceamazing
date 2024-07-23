package org.codeblessing.sourceamazing.schema.typemirror

data class ReturnMirror(
    val type: TypeMirror,
    override val annotations: List<AnnotationMirror> = emptyList(),
): AbstractMirror() {
    override fun longText(): String = TODO()

    override fun shortText(): String = TODO()

}