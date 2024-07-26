package org.codeblessing.sourceamazing.schema.typemirror

interface ParameterMirrorInterface: AbstractMirrorInterface {
    val name: String?
    val type: TypeMirrorInterface
    override val annotations: List<AnnotationMirror>

    fun withArgument(index: Int, parameterValue: Any?): ParameterMirrorWithArgument {
        return ParameterMirrorWithArgument(
            index = index,
            param = this,
            value = parameterValue,
        )
    }

    override fun longText(): String = name ?: "<anonymous>" // TODO fqn

    override fun shortText(): String = name ?: "<anonymous>"


}