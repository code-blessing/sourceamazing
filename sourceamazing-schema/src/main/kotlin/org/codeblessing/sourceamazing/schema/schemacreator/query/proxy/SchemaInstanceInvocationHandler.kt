package org.codeblessing.sourceamazing.schema.schemacreator.query.proxy

import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.conceptgraph.ConceptGraph
import org.codeblessing.sourceamazing.schema.proxy.InvocationHandlerHelper
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.toConceptName
import org.codeblessing.sourceamazing.schema.type.findAnnotation
import org.codeblessing.sourceamazing.schema.util.MethodUtil
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class SchemaInstanceInvocationHandler(private val conceptGraph: ConceptGraph): InvocationHandler  {


    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any? {
        val method = InvocationHandlerHelper.validateInvocationArguments(proxyOrNull, methodOrNull, argsOrNull)

        method.findAnnotation<QueryConcepts>()?.let {
            val conceptClasses = it.conceptClasses
            val conceptNamesToQuery = conceptClasses.map { it.toConceptName() }.toSet()
            val conceptNodes = conceptGraph.conceptsByConceptNames(conceptNamesToQuery)
            val proxyList = conceptNodes.map { conceptNode ->
                ProxyCreator.createProxy(
                    definitionClass = conceptNode.conceptName.clazz,
                    invocationHandler = ConceptInstanceInvocationHandler(conceptNode)
                ) }

            return@invoke MethodUtil.toMethodReturnType(method, proxyList)
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method)
    }
}
