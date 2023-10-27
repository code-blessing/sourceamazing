package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class DataCollectorConceptBuilderInvocationHandler(
    private val dataCollector: ConceptDataCollector,
    private val dataCollectorConceptBuilder: ConceptData
) : InvocationHandler {
    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any {
        if(DataCollectorInvocationHandlerHelper.isAddFacetAnnotated(method)) {
            val facetName = DataCollectorInvocationHandlerHelper.getFacetNameParameter(method, args)
            val facetValue = DataCollectorInvocationHandlerHelper.getFacetValueParameter(method, args)

            dataCollectorConceptBuilder.addOrReplaceFacetValue(facetName = facetName, facetValue = facetValue)
            return InvocationHandlerHelper.requiredProxy(proxy, method)
        } else if(DataCollectorInvocationHandlerHelper.isSetParentConceptAnnotated(method)) {
            val parentConceptIdentifier = DataCollectorInvocationHandlerHelper.getParentConceptIdentifierParameter(method, args)
            dataCollectorConceptBuilder.setParentConceptIdentifier(parentConceptIdentifier)
            return InvocationHandlerHelper.requiredProxy(proxy, method)
        } else if(DataCollectorInvocationHandlerHelper.isAddConceptAnnotated(method)) {
            val conceptDataBuilderClass = DataCollectorInvocationHandlerHelper.getConceptBuilderClazz(method)

            val conceptName = DataCollectorInvocationHandlerHelper.getConceptNameParameter(method, args)
            val conceptIdentifier = DataCollectorInvocationHandlerHelper.getConceptIdentifierParameter(method, args)
            val parentConceptIdentifier =
                DataCollectorInvocationHandlerHelper.getParentConceptIdentifierParameter(method, args)

            val conceptBuilder = dataCollector.existingOrNewConceptData(conceptName = conceptName, conceptIdentifier = conceptIdentifier, parentConceptIdentifier = parentConceptIdentifier)
            return ProxyCreator.createProxy(conceptDataBuilderClass.java, DataCollectorConceptBuilderInvocationHandler(dataCollector, conceptBuilder))
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method)
    }
}
