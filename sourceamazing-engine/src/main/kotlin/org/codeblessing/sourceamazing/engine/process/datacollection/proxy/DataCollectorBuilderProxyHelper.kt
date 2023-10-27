package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.ConceptBuilder
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import java.lang.reflect.Method

object DataCollectorBuilderProxyHelper {

    fun createBuilderProxy(
        method: Method,
        args: Array<out Any>,
        dataCollector: ConceptDataCollector,
        parentConceptIdentifier: ConceptIdentifier?
    ): Any {
        val conceptDataBuilderClass = DataCollectorInvocationHandlerHelper.getConceptBuilderClazz(method)

        val conceptName = DataCollectorInvocationHandlerHelper.getConceptNameParameter(method, args)
        val conceptIdentifier = DataCollectorInvocationHandlerHelper.getConceptIdentifierParameter(method, args)

        val conceptData = dataCollector.existingOrNewConceptData(conceptName = conceptName, conceptIdentifier = conceptIdentifier, parentConceptIdentifier = parentConceptIdentifier)
        val builder =  ProxyCreator.createProxy(conceptDataBuilderClass.java, DataCollectorConceptBuilderInvocationHandler(dataCollector, conceptData))

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