package org.codeblessing.sourceamazing.schema.typemirror

data class FakeFunctionTypeMirror(
    override val nullable: Boolean = false,
    override val functionMirror: FunctionMirrorInterface,
): FunctionTypeMirrorInterface {
    fun nullable(isNullable: Boolean): FakeFunctionTypeMirror {
        return copy(
            nullable = isNullable
        )
    }

    override fun provideMirror(): FunctionTypeMirrorInterface = this
}