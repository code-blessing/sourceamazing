package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

data class FunctionMirror (
    val functionName: String?,
    override val annotations: List<AnnotationMirror> = emptyList(),
    val receiverParameterType: ParameterMirror? = null,
    val instanceParameterType: ParameterMirror? = null,
    val parameters: List<ParameterMirror> = emptyList(),
    val returnType: ReturnMirror? = null,
): AbstractMirror(), MirrorProvider<FunctionMirror>, SignatureMirror {

    companion object {
        fun methodMirror(methodName: String = "UnnamedMethod"): FunctionMirror{
            return FunctionMirror(
                functionName = methodName
            )
        }
        fun anonymousFunctionMirror(): FunctionMirror{
            return FunctionMirror(
                functionName = null,
            )
        }
    }

    override fun provideMirror(): FunctionMirror = this

    override fun longText(): String = functionName ?: "<anonymousMethod>" // TODO fqn

    override fun shortText(): String  = functionName ?: "<anonymousMethod>"

    fun withMethodArguments(args: Array<out Any?>): List<ParameterMirrorWithArgument> {
        require(args.size == parameters.size) {
            "Argument size (${args.size}) must match size of ${parameters.size}"
        }
        return parameters
            .mapIndexed { index, parameterMirror -> parameterMirror.withArgument(index, args[index]) }
    }


    fun withAnnotation(annotation: AnnotationMirror): FunctionMirror {
        return this.copy(
            annotations = this.annotations + annotation
        )
    }

    fun withMethodName(methodName: String): FunctionMirror {
        return copy(
            functionName = methodName
        )
    }

    fun withReturnType(returnType: TypeMirror): FunctionMirror {
        return copy(
            returnType = ReturnMirror(returnType)
        )
    }

    fun withNoReturnType(): FunctionMirror {
        return copy(
            returnType = null
        )
    }

    fun withReturnType(returnClass: SignatureMirror, nullable: Boolean = false, vararg returnTypeAnnotations: AnnotationMirror): FunctionMirror {
        return copy(
            returnType = ReturnMirror(
                type = TypeMirror(
                    signatureMirror = returnClass.toMirrorProvider(),
                    nullable = nullable,
                ),
                annotations = returnTypeAnnotations.toList(),
            )
        )
    }

    fun withReceiverType(receiverType: ClassMirror): FunctionMirror {
        return copy(
            receiverParameterType = ParameterMirror(
                name = null,
                type = TypeMirror(
                    signatureMirror = receiverType.toMirrorProvider(),
                    nullable = false,
                ),
                annotations = emptyList(),
            )
        )
    }

    fun withParameter(parameterName: String, parameterType: TypeMirror): FunctionMirror {
        return copy(
            parameters = parameters + ParameterMirror(
                name = parameterName,
                type = parameterType,
            ),
        )
    }

    fun withParameter(parameterName: String, parameterClass: SignatureMirror, nullable: Boolean = false, vararg parameterAnnotation: AnnotationMirror): FunctionMirror {
        return copy(
            parameters = parameters + ParameterMirror(
                name = parameterName,
                type = TypeMirror(
                    signatureMirror = parameterClass.toMirrorProvider(),
                    nullable = nullable,
                ),
                annotations = parameterAnnotation.toList()
            ),
        )
    }
}