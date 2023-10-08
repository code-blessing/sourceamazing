package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import java.lang.reflect.Method
import kotlin.reflect.KClass

object DataCollectorInvocationHandlerHelper {

    fun isAddConceptAnnotated(method: Method?): Boolean {
        return validatedMethod(method).getAnnotation(AddConcept::class.java) != null
    }

    fun isAddFacetAnnotated(method: Method?): Boolean {
        return validatedMethod(method).getAnnotation(AddFacet::class.java) != null
    }

    fun isSetParentConceptAnnotated(method: Method?): Boolean {
        return validatedMethod(method).getAnnotation(SetParent::class.java) != null
    }

    fun getConceptNameParameter(method: Method?, args: Array<out Any>?): ConceptName {
        return getParameter(method, ConceptNameValue::class.java, ConceptName::class.java, args)
    }

    fun getConceptIdentifierParameter(method: Method?, args: Array<out Any>?): ConceptIdentifier {
        return getParameter(method, ConceptIdentifierValue::class.java, ConceptIdentifier::class.java, args)
    }

    fun getParentConceptIdentifierParameter(method: Method?, args: Array<out Any>?): ConceptIdentifier? {
        return getNullableParameter(method, ParentConceptIdentifierValue::class.java, ConceptIdentifier::class.java, args)
    }

    fun getConceptBuilderClazz(method: Method?): KClass<*> {
        return validatedMethod(method).getAnnotation(AddConcept::class.java).conceptBuilderClazz
    }

    fun getFacetNameParameter(method: Method?, args: Array<out Any>?): FacetName {
        return getParameter(method, FacetNameValue::class.java, FacetName::class.java, args)
    }

    fun getFacetValueParameter(method: Method?, args: Array<out Any>?): Any? {
        return getNullableParameter(method, FacetValue::class.java, Any::class.java, args)
    }

    private fun <T> getNullableParameter(method: Method?, annotation: Class<out Annotation>, type: Class<T>, args: Array<out Any>?): T? {
        // TODO validate that only one parameter is present
        // TODO handle case that no parameter is present

        val arguments = validatedArguments(method, args)

        for ((index, parameter) in validatedMethod(method).parameters.withIndex()) {
            if(parameter.getAnnotation(annotation) != null) {
                return arguments[index] as T?
            }
        }
        throw IllegalStateException("Method $method: No arguments found for annotation '$annotation' in $arguments")

    }
    private fun <T> getParameter(method: Method?, annotation: Class<out Annotation>, type: Class<T>, args: Array<out Any>?): T {
        return getNullableParameter(method, annotation, type, args)
            ?: throw IllegalStateException("Method $method: Arguments for annotation '$annotation' in $args was null.")
    }

    private fun validatedArguments(method: Method?, args: Array<out Any>?): Array<out Any> {
        val parameterCount = validatedMethod(method).parameterCount
        if(args == null) {
            throw IllegalStateException("No Arguments were provided for method $method in proxy $this.")
        }

        val argumentCount = args.size
        if(argumentCount != parameterCount) {
            throw IllegalStateException("The method $method in proxy $this expect $parameterCount arguments, but was $argumentCount.")
        }

        return args

    }


    private fun validatedMethod(method: Method?): Method {
        if(method == null) {
            throw IllegalStateException("Proxy $this can only handle methods, not field invocations.")
        }

        return method

    }
}
