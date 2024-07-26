package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

interface FunctionMirrorInterface: MirrorProvider<FunctionMirrorInterface>, SignatureMirror, AbstractMirrorInterface {
    val functionName: String?
    override val annotations: List<AnnotationMirror>
    val receiverParameterType: ParameterMirror?
    val instanceParameterType: ParameterMirror?
    val parameters: List<ParameterMirror>
    val returnType: ReturnMirror?

    override fun provideMirror(): FunctionMirrorInterface = this

    fun withMethodArguments(args: Array<out Any?>): List<ParameterMirrorWithArgument> {
        require(args.size == parameters.size) {
            "Argument size (${args.size}) must match size of ${parameters.size}"
        }
        return parameters
            .mapIndexed { index, parameterMirror -> parameterMirror.withArgument(index, args[index]) }
    }

}