package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.reflect.KClass

object DataCollectorInvocationHandlerHelper {

    fun getConceptNameParameter(method: Method, args: Array<out Any>): ConceptName {
        val conceptNameValueAnnotation = method.getAnnotation(ConceptNameValue::class.java)
        if(conceptNameValueAnnotation != null) {
            return ConceptName.of(conceptNameValueAnnotation.conceptName)
        }
        return getParameter(method, DynamicConceptNameValue::class.java, ConceptName::class.java, args)
    }

    fun getConceptIdentifierParameter(method: Method, args: Array<out Any>): ConceptIdentifier {
        // TODO support for random id annotation on method
        return getParameter(method, ConceptIdentifierValue::class.java, ConceptIdentifier::class.java, args)
    }

    fun getConceptBuilderClazz(method: Method): KClass<*> {
        return method.getAnnotation(AddConceptAndFacets::class.java).conceptBuilderClazz
    }

    fun getFacetName(parameter: Parameter): FacetName {
        return FacetName.of(parameter.getAnnotation(FacetValue::class.java).facetName)
    }

    fun <T> getConceptBuilderParameter(method: Method, args: Array<out Any>, clazz: Class<T>): T? {
        for ((index, parameter) in method.parameters.withIndex()) {
            if(parameter.getAnnotation(ConceptBuilder::class.java) != null) {
                return args[index] as T?
            }
        }
        return null;
    }

    fun <T> getNullableParameter(method: Method, annotation: Class<out Annotation>, type: Class<T>, args: Array<out Any>): T? {
        // TODO validate that only one parameter is present

        for ((index, parameter) in method.parameters.withIndex()) {
            if(parameter.getAnnotation(annotation) != null) {
                return args[index] as T?
            }
        }
        throw IllegalStateException("Method $method: No arguments found for annotation '$annotation' in $args")
    }

    fun <T> getParameter(method: Method, annotation: Class<out Annotation>, type: Class<T>, args: Array<out Any>): T {
        return getNullableParameter(method, annotation, type, args)
            ?: throw IllegalStateException("Method $method: Arguments for annotation '$annotation' in $args was null.")
    }

    fun <T> getNullableParameter(method: Method, parameter: Parameter, type: Class<T>, args: Array<out Any>): T? {
        for ((index, parameterOfIndex) in method.parameters.withIndex()) {
            if(parameterOfIndex == parameter) {
                return args[index] as T?
            }
        }
        throw IllegalStateException("Method $method: No arguments found for parameter '$parameter' in $args")
    }

}
