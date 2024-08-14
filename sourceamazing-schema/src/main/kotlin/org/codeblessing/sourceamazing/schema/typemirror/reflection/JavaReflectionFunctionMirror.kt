package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AbstractMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ParameterMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ReturnMirrorInterface
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

data class JavaReflectionFunctionMirror (
    private val memberFunction: KFunction<*>,
): AbstractMirror(), FunctionMirrorInterface {

    override val functionName: String = memberFunction.name
    override val annotations: List<AnnotationMirror> = JavaReflectionMirrorFactory.createAnnotationList(memberFunction.annotations)
    override val receiverParameterType: ParameterMirrorInterface? = memberFunction.parameters
        .filter { it.kind == KParameter.Kind.EXTENSION_RECEIVER }
        .map(::JavaReflectionParameterMirror)
        .firstOrNull()
    override val instanceParameterType: ParameterMirrorInterface? = memberFunction.parameters
        .filter { it.kind == KParameter.Kind.INSTANCE }
        .map(::JavaReflectionParameterMirror)
        .firstOrNull()
    override val parameters: List<ParameterMirrorInterface> = memberFunction.parameters
        .filter { it.kind == KParameter.Kind.VALUE }
        .map(::JavaReflectionParameterMirror)
    override val returnType: ReturnMirrorInterface = JavaReflectionReturnMirror(memberFunction.returnType)
}