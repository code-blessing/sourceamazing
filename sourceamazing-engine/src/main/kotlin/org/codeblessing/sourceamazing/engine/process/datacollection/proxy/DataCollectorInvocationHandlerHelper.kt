package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
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
        val numberOfParams = InvocationHandlerHelper.numberOfParamsAnnotatedWith(method, ConceptIdentifierValue::class.java)
        if(numberOfParams > 0) {
            if(numberOfParams > 1) {
                throw IllegalStateException("Method '$method' has to many parameters annotated " +
                        "with ${ConceptIdentifierValue::class.java}. " +
                        "Zero or one is allowed, was ${numberOfParams}.")
            }

            val conceptIdentifierValue = getNullableParameter(method, ConceptIdentifierValue::class.java, Any::class.java, args)
            if(conceptIdentifierValue != null) {
                return when(conceptIdentifierValue) {
                    is String -> ConceptIdentifier.of(conceptIdentifierValue)
                    is ConceptIdentifier -> conceptIdentifierValue
                    else -> throw IllegalStateException("Method '$method' has a parameter annotated with '${ConceptIdentifier::class.java}' " +
                            "where it's value is neither of type '${String::class.java}' nor '${ConceptIdentifier::class.java}' " +
                            "but '${conceptIdentifierValue}'.")
                }
            }
        }

        val autoRandomConceptIdentifier = method.getAnnotation(AutoRandomConceptIdentifier::class.java)
        if(autoRandomConceptIdentifier != null) {
            return ConceptIdentifier.random()
        }

        throw IllegalStateException("No concept identifier found in method '$method'. " +
                "Use the annotations '${ConceptIdentifierValue::class.java}' to manually declare one " +
                "or '${AutoRandomConceptIdentifier::class.java}' to add a random one. " +
                "Method arguments were: '${args.joinToString()}'")

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
