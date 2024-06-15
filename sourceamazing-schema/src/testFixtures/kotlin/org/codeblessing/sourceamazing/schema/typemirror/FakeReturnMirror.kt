package org.codeblessing.sourceamazing.schema.typemirror

data class FakeReturnMirror(
    override val type: TypeMirrorInterface,
    override val annotations: List<AnnotationMirror> = emptyList(),
): AbstractMirror(), ReturnMirrorInterface