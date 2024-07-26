package org.codeblessing.sourceamazing.schema.typemirror

data class ParameterMirror(
    override val name: String?,
    override val type: TypeMirror,
    override val annotations: List<AnnotationMirror> = emptyList(),
): AbstractMirror(), ParameterMirrorInterface {
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