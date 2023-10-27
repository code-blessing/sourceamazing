package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.AddConceptAndFacets
import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class DataCollectorInvocationHandler(private val dataCollector: ConceptDataCollector) : InvocationHandler {

    private val requiredMethodAnnotations = setOf(
        AddConceptAndFacets::class.java,
    )

    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any {

        InvocationHandlerHelper.requiredProxy(proxyOrNull, methodOrNull)
        val method: Method = InvocationHandlerHelper.validatedMethod(methodOrNull)
        val args: Array<out Any> = InvocationHandlerHelper.validatedArguments(methodOrNull, argsOrNull)


        if(InvocationHandlerHelper.isMethodAnnotatedWithExactlyOneOf(method, requiredMethodAnnotations)) {
            if(InvocationHandlerHelper.isMethodAnnotatedWith(method, AddConceptAndFacets::class.java)) {
                InvocationHandlerHelper.validateAllMethodParamsAnnotated(method)

                return DataCollectorBuilderProxyHelper.createBuilderProxy(
                    method = method,
                    args = args,
                    dataCollector = dataCollector,
                    parentConceptIdentifier = null, /* always null because we are in root data collector */
                )
            }
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method, requiredMethodAnnotations)
    }
}
