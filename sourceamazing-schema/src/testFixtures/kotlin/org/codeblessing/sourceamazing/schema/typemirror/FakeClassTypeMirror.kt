package org.codeblessing.sourceamazing.schema.typemirror

data class FakeClassTypeMirror(
    override val nullable: Boolean = false,
    override val classMirror: ClassMirrorInterface,
    override val genericTypeArguments: List<TypeMirrorInterface> = emptyList(),
): ClassTypeMirrorInterface {
    fun nullable(isNullable: Boolean): FakeClassTypeMirror {
        return copy(
            nullable = isNullable
        )
    }

    override fun provideMirror(): ClassTypeMirrorInterface = this
}