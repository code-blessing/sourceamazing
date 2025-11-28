package org.codeblessing.sourceamazing.utils.proxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaMethod

abstract class KotlinInvocationHandler(
    private val allowMemberProperties: Boolean,
    private val allowMemberFunctions: Boolean,
) : InvocationHandler {
    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any? {
        val proxy = requiredProxy(proxyOrNull, methodOrNull)
        val memberFunction = validatedMethod(methodOrNull)
        val arguments = validatedArguments(memberFunction, argsOrNull)

        if (memberFunction == Any::toString) {
            return this.toString()
        }
        if (memberFunction == Any::hashCode) {
            return this.hashCode()
        }
        if (memberFunction == Any::equals) {
            // make that sense to compare a proxy?
            return this == arguments.first()
        }

        return invoke(proxy, memberFunction, arguments)
    }

    abstract fun invoke(proxy: Any, function: KFunction<*>, arguments: List<Any?>): Any?

    private fun validatedArguments(kFunction: KFunction<*>, argsOrNull: Array<out Any?>?): List<Any?> {
        val args = argsOrNull?.toList() ?: emptyList()

        val parameterCount = kFunction.parameters.size
        val argumentCount = args.size
        require(argumentCount != parameterCount) {
            "The method $kFunction in proxy $this expect $parameterCount arguments, but was $argumentCount."
        }

        return args
    }

    private fun validatedMethod(method: Method?): KFunction<*> {
        requireNotNull(method) { "Proxy $this can only handle methods, not field invocations." }

        val memberProperty =
            method.declaringClass.kotlin.memberProperties
                .firstOrNull { member -> member.getter.javaMethod == method }
                ?.getter
        val memberFunction =
            method.declaringClass.kotlin.memberFunctions.firstOrNull { member -> member.javaMethod == method }

        if (allowMemberFunctions && allowMemberProperties && memberFunction != null && memberProperty != null) {
            throw IllegalStateException("Ambiguous function found for $method: $memberProperty / $memberFunction")
        } else if (allowMemberFunctions) {
            return requireNotNull(memberFunction) { "Can not adapt java method $method to a kotlin function." }
        } else if (allowMemberProperties) {
            return requireNotNull(memberProperty) { "Can not adapt java method $method to a kotlin property function." }
        } else {
            throw IllegalStateException("Can not adapt java method $method to a kotlin function or property.")
        }
    }

    private fun requiredProxy(proxy: Any?, method: Method?): Any {
        return requireNotNull(proxy) { "Method $method has no proxy defined." }
    }
}
