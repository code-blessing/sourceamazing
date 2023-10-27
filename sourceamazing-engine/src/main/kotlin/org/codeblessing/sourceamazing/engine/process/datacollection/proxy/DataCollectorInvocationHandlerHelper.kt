package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import java.lang.reflect.Method
import kotlin.reflect.KClass

object DataCollectorInvocationHandlerHelper {

    fun getConceptNameParameter(method: Method, args: Array<out Any>): ConceptName {
        // TODO support for presetConceptName annotation
        return getParameter(method, ConceptNameValue::class.java, ConceptName::class.java, args)
    }

    fun getConceptIdentifierParameter(method: Method, args: Array<out Any>): ConceptIdentifier {
        // TODO support for random id annotation on method
        return getParameter(method, ConceptIdentifierValue::class.java, ConceptIdentifier::class.java, args)
    }

    fun getConceptBuilderClazz(method: Method): KClass<*> {
        return method.getAnnotation(AddConcept::class.java).conceptBuilderClazz
    }

    fun getFacetNameParameter(method: Method, args: Array<out Any>): FacetName {
        // TODO support for presetFacetName annotation
        return getParameter(method, FacetNameValue::class.java, FacetName::class.java, args)
    }

    fun getFacetValueParameter(method: Method, args: Array<out Any>): Any? {
        return getNullableParameter(method, FacetValue::class.java, Any::class.java, args)
    }

    private fun <T> getNullableParameter(method: Method, annotation: Class<out Annotation>, type: Class<T>, args: Array<out Any>): T? {
        // TODO validate that only one parameter is present
        // TODO handle case that no parameter is present

        for ((index, parameter) in method.parameters.withIndex()) {
            if(parameter.getAnnotation(annotation) != null) {
                return args[index] as T?
            }
        }
        throw IllegalStateException("Method $method: No arguments found for annotation '$annotation' in $args")

    }
    private fun <T> getParameter(method: Method, annotation: Class<out Annotation>, type: Class<T>, args: Array<out Any>): T {
        return getNullableParameter(method, annotation, type, args)
            ?: throw IllegalStateException("Method $method: Arguments for annotation '$annotation' in $args was null.")
    }
}
