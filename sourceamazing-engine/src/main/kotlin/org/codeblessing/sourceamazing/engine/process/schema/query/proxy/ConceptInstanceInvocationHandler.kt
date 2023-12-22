package org.codeblessing.sourceamazing.engine.process.schema.query.proxy

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptNode
import org.codeblessing.sourceamazing.engine.process.util.AnnotationUtil
import org.codeblessing.sourceamazing.engine.process.util.MethodUtil
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class ConceptInstanceInvocationHandler(private val conceptNode: ConceptNode): InvocationHandler  {

    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any? {
        val method = InvocationHandlerHelper.validateInvocationArguments(proxyOrNull, methodOrNull, argsOrNull)

        if(AnnotationUtil.hasAnnotation(method, QueryFacetValue::class)) {
            val facetClass = AnnotationUtil.getAnnotation(method, QueryFacetValue::class).facetClass
            val facetNameToQuery = FacetName.of(facetClass)
            val facetValues = conceptNode.facetValues[facetNameToQuery] ?: throw IllegalStateException("Facet values not found for facet ${facetClass}.")

            val resultList = facetValues.map(::mapFacetValue)
            return MethodUtil.toMethodReturnType(method, resultList)
        }

        if(AnnotationUtil.hasAnnotation(method, QueryConceptIdentifierValue::class)) {
            return when(method.returnType.kotlin) {
                String::class -> conceptNode.conceptIdentifier.name
                ConceptIdentifier::class -> conceptNode.conceptIdentifier
                else -> throw IllegalStateException("Unsupported type for conceptIdentifier method.")
            }
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method)
    }

    private fun mapFacetValue(facetValue: Any): Any {
        return if(facetValue is ConceptNode) {
            ProxyCreator.createProxy(facetValue.conceptName.clazz, ConceptInstanceInvocationHandler(facetValue))
        } else {
            facetValue
        }
    }
}
