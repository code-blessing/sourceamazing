package org.codeblessing.sourceamazing.engine.proxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

object InvocationHandlerHelper {

    fun handleObjectMethodsOrThrow(
        invocationHandler: InvocationHandler,
        method: Method?,
        requiredMethodAnnotations: Set<Class<out Annotation>>
    ): Any {
        if(method != null) {
            if(method.name == "toString") {
                return invocationHandler.toString()
            }
            if(method.name == "hashCode") {
                return invocationHandler.hashCode()
            }
        }
        throw IllegalStateException("Method $method not annotated. Use exactly one of this annotations: $requiredMethodAnnotations")
    }

    fun isMethodAnnotatedWithExactlyOneOf(method: Method, annotations: Set<Class<out Annotation>>): Boolean {
        return annotations.filter { annotation ->  isMethodAnnotatedWith(method, annotation) }.size == 1
    }

    fun isMethodAnnotatedWith(method: Method, annotation: Class<out Annotation>): Boolean {
        return method.getAnnotation(annotation) != null
    }

    fun validatedArguments(method: Method?, argsOrNull: Array<out Any>?): Array<out Any> {
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

}
