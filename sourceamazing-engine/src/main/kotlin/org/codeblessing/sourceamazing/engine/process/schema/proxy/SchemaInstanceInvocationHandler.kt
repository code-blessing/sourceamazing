package org.codeblessing.sourceamazing.engine.process.schema.proxy

import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcepts
import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConceptsWithCommonBaseInterface
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptGraph
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptNode
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.reflect.KClass

class SchemaInstanceInvocationHandler(private val conceptGraph: ConceptGraph) : InvocationHandler {

    private val requiredMethodAnnotations = setOf(
        ChildConcepts::class.java,
        ChildConceptsWithCommonBaseInterface::class.java,
    )


    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any {
        InvocationHandlerHelper.requiredProxy(proxyOrNull, methodOrNull)
        val method: Method = InvocationHandlerHelper.validatedMethod(methodOrNull)
        InvocationHandlerHelper.validatedArguments(methodOrNull, argsOrNull)

        if(InvocationHandlerHelper.isMethodAnnotatedWithExactlyOneOf(method, requiredMethodAnnotations)) {
            if(InvocationHandlerHelper.isMethodAnnotatedWith(method, ChildConcepts::class.java)
                || InvocationHandlerHelper.isMethodAnnotatedWith(method, ChildConceptsWithCommonBaseInterface::class.java)) {

                return SchemaInvocationHandlerHelper.mapToProxy(method, conceptGraph) { interfaceClass: KClass<*>, childConceptNode: ConceptNode ->
                    ProxyCreator.createProxy(interfaceClass.java, SchemaConceptInstanceInvocationHandler(childConceptNode))
                }
            }
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method, requiredMethodAnnotations)
    }
}
