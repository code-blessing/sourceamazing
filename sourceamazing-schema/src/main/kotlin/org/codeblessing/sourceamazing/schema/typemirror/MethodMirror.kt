package org.codeblessing.sourceamazing.schema.typemirror

data class MethodMirror (
    val methodName: String?,
    override val annotations: List<AnnotationMirror> = emptyList(),
    val returnType: TypeMirror? = null,
    val parameters: List<ParameterMirror> = emptyList(),
): AbstractMirror() {

    companion object {
        fun methodMirror(methodName: String = "UnnamedMethod"): MethodMirror{
            return MethodMirror(
                methodName = methodName
            )
        }
        fun anonymousMethodMirror(): MethodMirror{
            return MethodMirror(
                methodName = null,
            )
        }
    }

    fun withMethodArguments(args: Array<out Any?>): List<ParameterMirrorWithArgument> {
        require(args.size == parameters.size) {
            "Argument size (${args.size}) must match size of ${parameters.size}"
        }
        return parameters
            .mapIndexed { index, parameterMirror -> parameterMirror.withArgument(index, args[index]) }
    }


    fun withAnnotation(annotation: AnnotationMirror): MethodMirror {
        return this.copy(
            annotations = this.annotations + annotation
        )
    }

    fun withMethodName(methodName: String): MethodMirror {
        return copy(
            methodName = methodName
        )
    }

    fun withReturnType(returnType: TypeMirror): MethodMirror {
        return copy(
            returnType = returnType
        )
    }

    fun withReturnType(returnClass: ClassMirror, nullable: Boolean = false, vararg returnTypeAnnotations: AnnotationMirror): MethodMirror {
        return copy(
            returnType = TypeMirror(
                classMirror = returnClass,
                nullable = nullable,
                annotations = returnTypeAnnotations.toList()
            )
        )
    }

    fun withParameter(parameterName: String, parametertype: TypeMirror): MethodMirror {
        return copy(
            parameters = parameters + ParameterMirror(
                name = parameterName,
                type = parametertype,
            ),
        )
    }

    fun withParameter(parameterName: String, parameterClass: ClassMirror, nullable: Boolean = false, vararg parameterAnnotation: AnnotationMirror): MethodMirror {
        return copy(
            parameters = parameters + ParameterMirror(
                name = parameterName,
                type = TypeMirror(
                    classMirror = parameterClass,
                    nullable = nullable,
                    annotations = parameterAnnotation.toList()
                ),
            ),
        )
    }

}