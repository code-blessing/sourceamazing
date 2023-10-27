package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import java.lang.reflect.Method

object DataCollectorBuilderProxyHelper {

    fun createBuilderProxy(method: Method?, args: Array<out Any>?, dataCollector: ConceptDataCollector): Any {
        val conceptDataBuilderClass = DataCollectorInvocationHandlerHelper.getConceptBuilderClazz(method)

        val conceptName = DataCollectorInvocationHandlerHelper.getConceptNameParameter(method, args)
        val conceptIdentifier = DataCollectorInvocationHandlerHelper.getConceptIdentifierParameter(method, args)
        val parentConceptIdentifier =
            DataCollectorInvocationHandlerHelper.getParentConceptIdentifierParameter(method, args)

        val conceptBuilder = dataCollector.existingOrNewConceptData(conceptName = conceptName, conceptIdentifier = conceptIdentifier, parentConceptIdentifier = parentConceptIdentifier)
        return ProxyCreator.createProxy(conceptDataBuilderClass.java, DataCollectorConceptBuilderInvocationHandler(dataCollector, conceptBuilder))
    }
}