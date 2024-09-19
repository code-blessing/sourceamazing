package org.codeblessing.sourceamazing.schema.fakereflection

import org.codeblessing.sourceamazing.schema.typemirror.FakeFunctionMirror
import org.codeblessing.sourceamazing.schema.typemirror.FakeFunctionTypeMirror
import org.codeblessing.sourceamazing.schema.typemirror.FakeParameterMirror
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVisibility

class FakeKFunction() : FakeKAnnotatedElement<FakeKFunction>(), KFunction<Any> {
    private var internalFunctionName: String? = null
    private val internalParameters: MutableList<KParameter> = mutableListOf()
    private var internalReturnType: KType = unitType()
    override val isAbstract: Boolean = false
    override val isExternal: Boolean = false
    override val isFinal: Boolean = false
    override val isInfix: Boolean = false
    override val isInline: Boolean = false
    override val isOpen: Boolean = false
    override val isOperator: Boolean = false
    override val isSuspend: Boolean = false
    override val name: String get() = internalFunctionName ?: ""
    override val parameters: List<KParameter> = internalParameters
    override val returnType: KType get() = internalReturnType
    override val typeParameters: List<KTypeParameter> = emptyList()
    override val visibility: KVisibility? = null

    override fun call(vararg args: Any?): Any {
        throw UnsupportedOperationException("call a FakeKFunction is not supported")
    }

    override fun callBy(args: Map<KParameter, Any?>): Any {
        throw UnsupportedOperationException("callBy a FakeKFunction is not supported")
    }

    private fun unitType(): KType {
        return FakeKType.createClassType(Unit::class, nullable = false)
    }

    companion object {
        fun methodMirror(methodName: String = "UnnamedMethod"): FakeKFunction {
            return FakeKFunction().withMethodName(methodName)
        }
        fun anonymousFunctionMirror(): FakeKFunction {
            return FakeKFunction()
        }
    }

    fun withMethodName(methodName: String): FakeKFunction {
        this.internalFunctionName = methodName
        return this
    }

    fun withNoReturnType(): FakeKFunction {
        this.internalReturnType = unitType()
        return this
    }

    fun withReturnType(returnClass: KClass<*>, nullable: Boolean = false, vararg returnTypeAnnotations: Annotation): FakeKFunction {
        this.internalReturnType = FakeKType.createClassType(returnClass, nullable).withAnnotations(*returnTypeAnnotations)
        return this
    }

    fun withReceiverType(receiverType: KType): FakeKFunction {
        val param = FakeKParameter(0, KParameter.Kind.EXTENSION_RECEIVER, null, receiverType)
        internalParameters.add(0, param)
        return this
    }

    fun withReceiverType(receiverClass: KClass<*>): FakeKFunction {
        val classType = FakeKType.createClassType(receiverClass)
        return withReceiverType(classType)
    }

    fun withParameter(parameterName: String, parameterType: KType): FakeKFunction {
        val param = FakeKParameter(internalParameters.size, KParameter.Kind.VALUE, parameterName, parameterType)
        internalParameters.add(param)
        return this
    }

    fun withParameter(parameterName: String, parameterClass: KClass<*>, nullable: Boolean = false, vararg parameterAnnotations: Annotation): FakeKFunction {
        val parameterType: FakeKType = FakeKType.createClassType(parameterClass, nullable).withAnnotations(*parameterAnnotations)
        val param = FakeKParameter(internalParameters.size, KParameter.Kind.VALUE, parameterName, parameterType)
        internalParameters.add(param)
        return this
    }

    fun withParameter(parameterName: String, parameterFunction: KFunction<*>, nullable: Boolean = false, vararg parameterAnnotations: Annotation): FakeKFunction {
        val parameterType: FakeKType = FakeKType.createFunctionType(parameterFunction, nullable).withAnnotations(*parameterAnnotations)
        val param = FakeKParameter(internalParameters.size, KParameter.Kind.VALUE, parameterName, parameterType)
        internalParameters.add(param)
        return this
    }
}