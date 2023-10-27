package org.codeblessing.sourceamazing.engine.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Parameter

object InvocationHandlerHelper {

    private val parameterAnnotations: Set<Class<out Annotation>> = setOf(
        ConceptBuilder::class.java,
        ConceptIdentifierValue::class.java,
        DynamicConceptNameValue::class.java,
        DynamicFacetNameValue::class.java,
        DynamicFacetValue::class.java,
        FacetValue::class.java,
    )

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

    fun isParamAnnotatedWith(parameter: Parameter, annotation: Class<out Annotation>): Boolean {
        return parameter.getAnnotation(annotation) != null
    }

    fun isParamAnnotatedWith(parameter: Parameter, annotations: Set<Class<out Annotation>>): Boolean {
        return annotations.any { annotation -> isParamAnnotatedWith(parameter, annotation) }
    }

    fun isAllMethodParamsAnnotated(method: Method): Boolean {
        return method.parameters.all { parameter -> isParamAnnotatedWith(parameter, parameterAnnotations) }
    }

    fun numberOfParamsAnnotatedWith(method: Method, annotation: Class<out Annotation>): Int {
        return paramsAnnotatedWith(method, annotation).size
    }

    fun paramsAnnotatedWith(method: Method, annotation: Class<out Annotation>): List<Parameter> {
        return method.parameters.filter { parameter -> isParamAnnotatedWith(parameter, annotation) }.toList()
    }


    fun validateAllMethodParamsAnnotated(method: Method) {
        if(!isAllMethodParamsAnnotated(method)) {
            throw IllegalStateException("Method $method has not all parameters annotated with a param annotation ($parameterAnnotations).")
        }
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
