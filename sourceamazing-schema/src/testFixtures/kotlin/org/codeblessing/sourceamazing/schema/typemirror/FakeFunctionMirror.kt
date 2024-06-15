package org.codeblessing.sourceamazing.schema.typemirror

data class FakeFunctionMirror (
    override val functionName: String?,
    override val annotations: List<AnnotationMirror> = emptyList(),
    override val receiverParameterType: FakeParameterMirror? = null,
    override val instanceParameterType: FakeParameterMirror? = null,
    override val valueParameters: List<FakeParameterMirror> = emptyList(),
    override val returnType: FakeReturnMirror? = null,
): AbstractMirror(), FunctionMirrorInterface {

    companion object {
        fun methodMirror(methodName: String = "UnnamedMethod"): FakeFunctionMirror {
            return FakeFunctionMirror(
                functionName = methodName
            )
        }
        fun anonymousFunctionMirror(): FakeFunctionMirror {
            return FakeFunctionMirror(
                functionName = null,
            )
        }
    }

    fun withAnnotation(annotation: AnnotationMirror): FakeFunctionMirror {
        return this.copy(
            annotations = this.annotations + annotation
        )
    }

    fun withMethodName(methodName: String): FakeFunctionMirror {
        return copy(
            functionName = methodName
        )
    }

    fun withNoReturnType(): FakeFunctionMirror {
        return copy(
            returnType = null
        )
    }

    fun withReturnType(returnClass: ClassMirrorInterface, nullable: Boolean = false, vararg returnTypeAnnotations: AnnotationMirror): FakeFunctionMirror {
        return copy(
            returnType = FakeReturnMirror(
                type = FakeClassTypeMirror(
                    classMirror = returnClass,
                    nullable = nullable,
                ),
                annotations = returnTypeAnnotations.toList(),
            )
        )
    }

    fun withReceiverType(receiverType: ClassMirrorInterface): FakeFunctionMirror {
        return copy(
            receiverParameterType = FakeParameterMirror(
                name = null,
                type = FakeClassTypeMirror(
                    classMirror = receiverType,
                    nullable = false,
                ),
                annotations = emptyList(),
            )
        )
    }

    fun withParameter(parameterName: String, parameterType: TypeMirrorInterface): FakeFunctionMirror {
        return copy(
            valueParameters = valueParameters + FakeParameterMirror(
                name = parameterName,
                type = parameterType,
            ),
        )
    }

    fun withParameter(parameterName: String, parameterClass: ClassMirrorInterface, nullable: Boolean = false, vararg parameterAnnotation: AnnotationMirror): FakeFunctionMirror {
        return copy(
            valueParameters = valueParameters + FakeParameterMirror(
                name = parameterName,
                type = FakeClassTypeMirror(
                    classMirror = parameterClass,
                    nullable = nullable,
                ),
                annotations = parameterAnnotation.toList()
            ),
        )
    }

    fun withParameter(parameterName: String, parameterFunction: FunctionMirrorInterface, nullable: Boolean = false, vararg parameterAnnotation: AnnotationMirror): FakeFunctionMirror {
        return copy(
            valueParameters = valueParameters + FakeParameterMirror(
                name = parameterName,
                type = FakeFunctionTypeMirror(
                    functionMirror = parameterFunction,
                    nullable = nullable,
                ),
                annotations = parameterAnnotation.toList()
            ),
        )
    }
}