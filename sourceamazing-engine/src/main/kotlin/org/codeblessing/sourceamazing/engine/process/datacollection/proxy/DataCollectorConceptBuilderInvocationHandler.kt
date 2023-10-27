package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.AddConcept
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.AddFacet
import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class DataCollectorConceptBuilderInvocationHandler(
    private val dataCollector: ConceptDataCollector,
    private val conceptData: ConceptData,
) : InvocationHandler {

    private val requiredMethodAnnotations = setOf(
        AddFacet::class.java,
        AddConcept::class.java,
    )

    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any {
        val proxy: Any = InvocationHandlerHelper.requiredProxy(proxyOrNull, methodOrNull)
        val method: Method = InvocationHandlerHelper.validatedMethod(methodOrNull)
        val args: Array<out Any> = InvocationHandlerHelper.validatedArguments(methodOrNull, argsOrNull)

        if(InvocationHandlerHelper.isMethodAnnotatedWithExactlyOneOf(method, requiredMethodAnnotations)) {

            if(InvocationHandlerHelper.isMethodAnnotatedWith(method, AddFacet::class.java)) {
                val facetName = DataCollectorInvocationHandlerHelper.getFacetNameParameter(method, args)
                val facetValue = DataCollectorInvocationHandlerHelper.getFacetValueParameter(method, args)
                conceptData.addOrReplaceFacetValue(facetName = facetName, facetValue = facetValue)
                return InvocationHandlerHelper.requiredProxy(proxy, method)
            } else if(InvocationHandlerHelper.isMethodAnnotatedWith(method, AddConcept::class.java)) {
                return DataCollectorBuilderProxyHelper.createBuilderProxy(method, args, dataCollector, parentConceptIdentifier = conceptData.conceptIdentifier)
            }
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method, requiredMethodAnnotations)
    }
}
