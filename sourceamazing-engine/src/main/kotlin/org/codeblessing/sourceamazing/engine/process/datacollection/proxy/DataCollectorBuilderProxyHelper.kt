package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.ConceptBuilder
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.DynamicFacetNameValue
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.DynamicFacetValue
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.FacetValue
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import java.lang.reflect.Method

object DataCollectorBuilderProxyHelper {

    fun handleFacetData(
        method: Method,
        args: Array<out Any>,
        conceptData: ConceptData,
    ) {

        val numberOfDynamicFacetNameValueParams = InvocationHandlerHelper.numberOfParamsAnnotatedWith(method, DynamicFacetNameValue::class.java)
        if(numberOfDynamicFacetNameValueParams > 0) {
            if(numberOfDynamicFacetNameValueParams > 1) {
                throw IllegalStateException("Method '$method' has more than one parameter annotated with '${DynamicFacetNameValue::class.java}'.")
            }

            if(InvocationHandlerHelper.numberOfParamsAnnotatedWith(method, DynamicFacetValue::class.java) != 1) {
                throw IllegalStateException("Method '$method' has parameter annotated with '${DynamicFacetNameValue::class.java}' and " +
                        "must have exactly one corresponding parameter annotated with '${DynamicFacetValue::class.java}'.")
            }

            val facetName = DataCollectorInvocationHandlerHelper.getParameter(method, DynamicFacetNameValue::class.java, FacetName::class.java, args)
            val facetValue = DataCollectorInvocationHandlerHelper.getNullableParameter(method, DynamicFacetValue::class.java, Any::class.java, args)
            conceptData.addOrReplaceFacetValue(facetName = facetName, facetValue = facetValue)
        }

        InvocationHandlerHelper.paramsAnnotatedWith(method, FacetValue::class.java).forEach { parameter ->
            val facetName = DataCollectorInvocationHandlerHelper.getFacetName(parameter)
            val facetValue = DataCollectorInvocationHandlerHelper.getNullableParameter(method, parameter, Any::class.java, args)
            conceptData.addOrReplaceFacetValue(facetName = facetName, facetValue = facetValue)
        }
    }

    fun createBuilderProxy(
        method: Method,
        args: Array<out Any>,
        dataCollector: ConceptDataCollector,
        parentConceptIdentifier: ConceptIdentifier?
    ): Any {
        val conceptDataBuilderClass = DataCollectorInvocationHandlerHelper.getConceptBuilderClazz(method)

        val conceptName = DataCollectorInvocationHandlerHelper.getConceptNameParameter(method, args)
        val conceptIdentifier = DataCollectorInvocationHandlerHelper.getConceptIdentifierParameter(method, args)

        val newConceptData = dataCollector.existingOrNewConceptData(conceptName = conceptName, conceptIdentifier = conceptIdentifier, parentConceptIdentifier = parentConceptIdentifier)
        handleFacetData(method, args, newConceptData)
        val builder =  ProxyCreator.createProxy(conceptDataBuilderClass.java, DataCollectorConceptBuilderInvocationHandler(dataCollector, newConceptData))
        val conceptBuilderFunctionParameter = DataCollectorInvocationHandlerHelper.getConceptBuilderParameter(method, args, conceptDataBuilderClass.java)

        if(conceptBuilderFunctionParameter != null) {
            val function: (Any) -> Unit = try {
                conceptBuilderFunctionParameter as (Any) -> Unit
            } catch (ex: Exception) {
                throw IllegalStateException("Could not cast builder parameter marked with '${ConceptBuilder::class.java}' in method '$method'. " +
                        "This must be a function receiving exactly one argument (the builder) and returning nothing, but was ${conceptBuilderFunctionParameter}.", ex)
            }
            function(builder)
        }
        return builder
    }
}