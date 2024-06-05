package org.codeblessing.sourceamazing.schema.proxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

object InvocationHandlerHelper {

    fun validateInvocationArguments(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any?>?): Method {
        requiredProxy(proxyOrNull, methodOrNull)
        val method: Method = validatedMethod(methodOrNull)
        validatedArguments(methodOrNull, argsOrNull)
        return method
    }

    fun validatedArguments(method: Method?, argsOrNull: Array<out Any?>?): Array<out Any?> {
        val args = argsOrNull ?: emptyArray()

        val parameterCount = validatedMethod(method).parameterCount
        val argumentCount = args.size
        if(argumentCount != parameterCount) {
            throw IllegalStateException("The method $method in proxy $this expect $parameterCount arguments, but was $argumentCount.")
        }

        return args
    }

    fun validatedMethod(method: Method?): Method {
        if(method == null) {
            throw IllegalStateException("Proxy $this can only handle methods, not field invocations.")
        }

        return method

    }

    fun requiredProxy(proxy: Any?, method: Method?): Any {
        if(proxy != null) {
            return proxy
        }
        throw IllegalStateException("Method $method has no proxy defined.")
    }

    fun handleObjectMethodsOrThrow(
        invocationHandler: InvocationHandler,
        method: Method?,
    ): Any {
        if(method != null) {
            if(method.name == ::toString.name) {
                return invocationHandler.toString()
            }
            if(method.name == ::hashCode.name) {
                return invocationHandler.hashCode()
            }
        }
        throw IllegalStateException("Method $method was not handled.")
    }
}