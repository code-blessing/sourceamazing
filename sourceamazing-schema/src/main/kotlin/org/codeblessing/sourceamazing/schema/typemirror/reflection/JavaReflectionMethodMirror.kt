package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AbstractMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ParameterMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ReturnMirrorInterface
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

data class JavaReflectionMethodMirror (
    private val memberFunction: KFunction<*>,
): AbstractMirror(), FunctionMirrorInterface {

    override val functionName: String = memberFunction.name
    override val annotations: List<AnnotationMirror> = JavaReflectionMirrorFactory.createAnnotationList(memberFunction.annotations)
    override val receiverParameterType: ParameterMirrorInterface? = memberFunction.parameters
        .filter { it.kind == KParameter.Kind.EXTENSION_RECEIVER }
        .map(::JavaReflectionMethodParameterMirror)
        .firstOrNull()
    override val instanceParameterType: ParameterMirrorInterface? = memberFunction.parameters
        .filter { it.kind == KParameter.Kind.INSTANCE }
        .map(::JavaReflectionMethodParameterMirror)
        .firstOrNull()
    override val valueParameters: List<ParameterMirrorInterface> = memberFunction.parameters
        .filter { it.kind == KParameter.Kind.VALUE }
        .map(::JavaReflectionMethodParameterMirror)
    override val returnType: ReturnMirrorInterface? = if(isUnitType(memberFunction.returnType)) null else JavaReflectionReturnMirror(memberFunction.returnType)

    private fun isUnitType(returnType: KType): Boolean {
        return returnType.jvmErasure == Unit::class
    }
}