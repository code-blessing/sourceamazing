package org.codeblessing.sourceamazing.engine.process.schema.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.AddConcept
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.SetParent
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.annotations.*
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptNode
import org.codeblessing.sourceamazing.engine.process.datacollection.proxy.DataCollectorInvocationHandlerHelper
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.reflect.KClass

class SchemaConceptInstanceInvocationHandler(private val conceptNode: ConceptNode) : InvocationHandler {

    private val requiredMethodAnnotations = setOf(
        ChildConcepts::class.java,
        ChildConceptsWithCommonBaseInterface::class.java,
        Facet::class.java,
        ConceptId::class.java,
        ParentConcept::class.java,
    )

    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any? {
        InvocationHandlerHelper.requiredProxy(proxyOrNull, methodOrNull)
        val method: Method = InvocationHandlerHelper.validatedMethod(methodOrNull)
        InvocationHandlerHelper.validatedArguments(methodOrNull, argsOrNull)

        if(InvocationHandlerHelper.isMethodAnnotatedWithExactlyOneOf(method, requiredMethodAnnotations)) {

            if(InvocationHandlerHelper.isMethodAnnotatedWith(method, ChildConcepts::class.java)
                || InvocationHandlerHelper.isMethodAnnotatedWith(method, ChildConceptsWithCommonBaseInterface::class.java)) {

                return SchemaInvocationHandlerHelper.mapToProxy(method, conceptNode) { interfaceClass: KClass<*>, childConceptNode: ConceptNode ->
                    ProxyCreator.createProxy(interfaceClass.java, SchemaConceptInstanceInvocationHandler(childConceptNode))
                }
            }

            if(InvocationHandlerHelper.isMethodAnnotatedWith(method, Facet::class.java)) {
                val facetName = SchemaInvocationHandlerHelper.getInputFacetName(method)
                val facetValue = conceptNode.facetValues[facetName]
                return if(facetValue is ConceptNode) {
                    val referencedInterfaceClass = method.returnType
                    ProxyCreator.createProxy(referencedInterfaceClass, SchemaConceptInstanceInvocationHandler(facetValue))
                } else {
                    facetValue
                }
            }

            if(InvocationHandlerHelper.isMethodAnnotatedWith(method, ConceptId::class.java)) {
                return when(method.returnType.kotlin) {
                    String::class -> conceptNode.conceptIdentifier.name
                    ConceptIdentifier::class -> conceptNode.conceptIdentifier
                    else -> throw IllegalStateException("Unsupported type for conceptIdentifier method.")
                }
            }

            if(InvocationHandlerHelper.isMethodAnnotatedWith(method, ParentConcept::class.java)) {
                val parentConceptNode = conceptNode.parentConceptNode ?: throw IllegalStateException("Parent concept node was null.")
                val parentInterfaceClass = method.returnType
                return ProxyCreator.createProxy(parentInterfaceClass, SchemaConceptInstanceInvocationHandler(parentConceptNode))
            }
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method, requiredMethodAnnotations)
    }
}
