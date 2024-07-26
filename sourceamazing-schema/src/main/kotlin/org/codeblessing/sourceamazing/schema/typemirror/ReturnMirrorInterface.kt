package org.codeblessing.sourceamazing.schema.typemirror

interface ReturnMirrorInterface: AbstractMirrorInterface {
    val type: TypeMirrorInterface
    override val annotations: List<AnnotationMirror>

    override fun longText(): String = "impl" // TODO

    override fun shortText(): String = "impl" // TODO
}