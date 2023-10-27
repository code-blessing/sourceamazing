package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class DataCollectorInvocationHandler(private val dataCollector: ConceptDataCollector) : InvocationHandler {
    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any {
        if(DataCollectorInvocationHandlerHelper.isAddConceptAnnotated(method)) {
            return DataCollectorBuilderProxyHelper.createBuilderProxy(method, args, dataCollector)
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method)

    }
}
