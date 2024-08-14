package org.codeblessing.sourceamazing.schema.typemirror

data class FakeParameterMirror(
    override val name: String?,
    override val type: TypeMirrorInterface,
    override val annotations: List<AnnotationMirror> = emptyList(),
): AbstractMirror(), ParameterMirrorInterface