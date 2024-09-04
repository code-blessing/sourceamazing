package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

interface FunctionMirrorInterface: MirrorProvider<FunctionMirrorInterface>, AbstractMirrorInterface {
    val functionName: String?
    override val annotations: List<AnnotationMirror>
    val receiverParameterType: ParameterMirrorInterface?
    val instanceParameterType: ParameterMirrorInterface?
    val valueParameters: List<ParameterMirrorInterface>
    val returnType: ReturnMirrorInterface?

    override fun provideMirror(): FunctionMirrorInterface = this

    fun withMethodArguments(args: Array<out Any?>): List<ParameterMirrorWithArgument> {
        require(args.size == valueParameters.size) {
            "Argument size (${args.size}) must match size of ${valueParameters.size}"
        }
        return valueParameters
            .mapIndexed { index, parameterMirror -> parameterMirror.withArgument(index, args[index]) }
    }

    override fun longText(): String = functionName ?: "<anonymousMethod>" // TODO fqn

    override fun shortText(): String  = functionName ?: "<anonymousMethod>"
}