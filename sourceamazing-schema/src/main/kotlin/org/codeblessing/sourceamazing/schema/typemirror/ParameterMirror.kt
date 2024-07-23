package org.codeblessing.sourceamazing.schema.typemirror

data class ParameterMirror(
    val name: String?,
    val type: TypeMirror,
    override val annotations: List<AnnotationMirror> = emptyList(),
): AbstractMirror() {
    override fun longText(): String = name ?: "<anonymous>" // TODO fqn

    override fun shortText(): String = name ?: "<anonymous>"

    fun withArgument(index: Int, parameterValue: Any?): ParameterMirrorWithArgument {
        return ParameterMirrorWithArgument(
            index = index,
            param = this,
            value = parameterValue,
        )
    }
}