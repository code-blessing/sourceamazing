package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.AddConceptAndFacets
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.AddFacets
import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class DataCollectorConceptBuilderInvocationHandler(
    private val dataCollector: ConceptDataCollector,
    private val conceptData: ConceptData,
) : InvocationHandler {

    private val requiredMethodAnnotations = setOf(
        AddFacets::class.java,
        AddConceptAndFacets::class.java,
    )

    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any {
        val proxy: Any = InvocationHandlerHelper.requiredProxy(proxyOrNull, methodOrNull)
        val method: Method = InvocationHandlerHelper.validatedMethod(methodOrNull)
        val args: Array<out Any> = InvocationHandlerHelper.validatedArguments(methodOrNull, argsOrNull)

        if(InvocationHandlerHelper.isMethodAnnotatedWithExactlyOneOf(method, requiredMethodAnnotations)) {
            InvocationHandlerHelper.validateAllMethodParamsAnnotated(method)

            if(InvocationHandlerHelper.isMethodAnnotatedWith(method, AddFacets::class.java)) {
                DataCollectorBuilderProxyHelper.handleFacetData(
                    method = method,
                    args = args,
                    conceptData = conceptData
                )
                return proxy
            } else if(InvocationHandlerHelper.isMethodAnnotatedWith(method, AddConceptAndFacets::class.java)) {
                return DataCollectorBuilderProxyHelper.createBuilderProxy(
                    method = method,
                    args = args,
                    dataCollector = dataCollector,
                    parentConceptIdentifier = conceptData.conceptIdentifier
                )
            }
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method, requiredMethodAnnotations)
    }
}
