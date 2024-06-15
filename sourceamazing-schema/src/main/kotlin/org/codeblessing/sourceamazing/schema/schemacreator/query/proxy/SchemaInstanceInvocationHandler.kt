package org.codeblessing.sourceamazing.schema.schemacreator.query.proxy

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.conceptgraph.ConceptGraph
import org.codeblessing.sourceamazing.schema.proxy.InvocationHandlerHelper
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.typemirror.MirrorFactory
import org.codeblessing.sourceamazing.schema.typemirror.QueryConceptsAnnotationMirror
import org.codeblessing.sourceamazing.schema.util.MethodUtil
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class SchemaInstanceInvocationHandler(private val conceptGraph: ConceptGraph): InvocationHandler  {


    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any? {
        val method = InvocationHandlerHelper.validateInvocationArguments(proxyOrNull, methodOrNull, argsOrNull)

        val methodMirror = MirrorFactory.convertToMethodMirror(method)

        methodMirror.getAnnotationMirrorOrNull(QueryConceptsAnnotationMirror::class)?.let {
            val conceptClasses = it.concepts
            val conceptNamesToQuery = conceptClasses.map { ConceptName.of(it.provideMirror()) }.toSet()
            val conceptNodes = conceptGraph.conceptsByConceptNames(conceptNamesToQuery)
            val proxyList = conceptNodes.map { conceptNode ->
                ProxyCreator.createProxy(
                    definitionClass = conceptNode.conceptName.clazz.convertToKClass(),
                    invocationHandler = ConceptInstanceInvocationHandler(conceptNode)
                ) }

            return MethodUtil.toMethodReturnType(method, proxyList)
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method)
    }
}
