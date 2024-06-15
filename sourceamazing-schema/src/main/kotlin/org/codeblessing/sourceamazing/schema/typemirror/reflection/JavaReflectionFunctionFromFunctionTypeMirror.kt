package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AbstractMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ParameterMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ReturnMirrorInterface
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.jvm.jvmErasure

/**
 * An anonymous function passed as parameter, field or return type
 */
data class JavaReflectionFunctionFromFunctionTypeMirror (
    private val kType: KType,
): AbstractMirror(), FunctionMirrorInterface {

    override val functionName: String? = null
    override val annotations: List<AnnotationMirror> = JavaReflectionMirrorFactory.createAnnotationList(kType.annotations)
    override val receiverParameterType: ParameterMirrorInterface? = if(isExtensionFunction()) kType.arguments.firstOrNull()?.let { toParameterMirrorInterface(it) } else null
    override val instanceParameterType: ParameterMirrorInterface? = null
    override val valueParameters: List<ParameterMirrorInterface> = kType.arguments
        .subList(if(isExtensionFunction()) 1 else 0, kType.arguments.size - 1)
        .map(this::toParameterMirrorInterface)
    override val returnType: ReturnMirrorInterface? = extractReturnType()

    private fun isExtensionFunction(): Boolean {
        return kType.annotations.any { it.annotationClass == ExtensionFunctionType::class }
    }

    private fun toParameterMirrorInterface(argument: KTypeProjection): ParameterMirrorInterface {
        val type = requireNotNull(argument.type) {
            "Argument $argument in type $kType as null (=a STAR projection)"
        }
        return JavaReflectionAnonymousFunctionParameterMirror(type)
    }

    private fun extractReturnType(): ReturnMirrorInterface? {
        val lastArgument = kType.arguments.lastOrNull() ?: return null
        val lastArgumentType = lastArgument.type ?: return null
        if(isUnitType(lastArgumentType)) {
            return null
        }
        return JavaReflectionReturnMirror(lastArgumentType)
    }
    private fun isUnitType(returnType: KType): Boolean {
        return returnType.jvmErasure == Unit::class
    }
}