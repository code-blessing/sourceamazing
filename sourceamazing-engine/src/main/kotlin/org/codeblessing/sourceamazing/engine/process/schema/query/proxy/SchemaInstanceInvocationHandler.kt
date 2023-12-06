package org.codeblessing.sourceamazing.engine.process.schema.query.proxy

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConcepts
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptGraph
import org.codeblessing.sourceamazing.engine.process.util.AnnotationUtil
import org.codeblessing.sourceamazing.engine.process.util.MethodUtil
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class SchemaInstanceInvocationHandler(private val conceptGraph: ConceptGraph): InvocationHandler  {


    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any? {
        val method = InvocationHandlerHelper.validateInvocationArguments(proxyOrNull, methodOrNull, argsOrNull)

        if(AnnotationUtil.hasAnnotation(method, QueryConcepts::class)) {
            val conceptClasses = AnnotationUtil.getAnnotation(method, QueryConcepts::class).conceptClasses
            val conceptNamesToQuery = conceptClasses.map { ConceptName.of(it) }.toSet()
            val conceptNodes = conceptGraph.conceptsByConceptNames(conceptNamesToQuery)
            val proxyList = conceptNodes.map { conceptNode ->  ProxyCreator.createProxy(conceptNode.conceptName.clazz, ConceptInstanceInvocationHandler(conceptNode)) }

            return MethodUtil.toMethodReturnType(method, proxyList)
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method)
    }
}
