package org.codeblessing.sourceamazing.engine.process.schema.proxy

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcept
import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConceptWithCommonBaseInterface
import org.codeblessing.sourceamazing.api.process.schema.annotations.ConceptId
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptNode
import org.codeblessing.sourceamazing.engine.process.schema.SchemaAnnotationConst
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import java.lang.reflect.Method
import kotlin.reflect.KClass

class SchemaConceptInstanceInvocationHandler(private val conceptNode: ConceptNode): AbstractSchemaInvocationHandler() {

    private val requiredMethodAnnotations = SchemaAnnotationConst.supportedConceptAnnotations

    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any? {
        val method = validateArguments(proxyOrNull, methodOrNull, argsOrNull)

        if(InvocationHandlerHelper.isMethodAnnotatedWithExactlyOneOf(method, requiredMethodAnnotations)) {
            handleCommonAnnotations(method, conceptNode)?.let { return it }

            if(InvocationHandlerHelper.isMethodAnnotatedWithExactlyOneOf(method, SchemaAnnotationConst.singleInstanceChildConceptAnnotations)) {
                return SchemaInvocationHandlerHelper.mapToSingleProxy(method, conceptNode) { interfaceClass: KClass<*>, childConceptNode: ConceptNode ->
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
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method, requiredMethodAnnotations)
    }
}
