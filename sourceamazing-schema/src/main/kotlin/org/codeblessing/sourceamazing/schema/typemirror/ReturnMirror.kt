package org.codeblessing.sourceamazing.schema.typemirror

data class ReturnMirror(
    override val type: TypeMirror,
    override val annotations: List<AnnotationMirror> = emptyList(),
): AbstractMirror(), ReturnMirrorInterface {
    override fun longText(): String = TODO()

    override fun shortText(): String = TODO()

}