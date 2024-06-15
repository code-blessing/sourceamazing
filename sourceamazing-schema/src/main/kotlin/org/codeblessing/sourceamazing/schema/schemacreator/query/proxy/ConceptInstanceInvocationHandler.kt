package org.codeblessing.sourceamazing.schema.schemacreator.query.proxy

import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.conceptgraph.ConceptNode
import org.codeblessing.sourceamazing.schema.proxy.InvocationHandlerHelper
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.toFacetName
import org.codeblessing.sourceamazing.schema.type.findAnnotation
import org.codeblessing.sourceamazing.schema.util.MethodUtil
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class ConceptInstanceInvocationHandler(private val conceptNode: ConceptNode): InvocationHandler  {

    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any? {
        val method = InvocationHandlerHelper.validateInvocationArguments(proxyOrNull, methodOrNull, argsOrNull)

        method.findAnnotation<QueryFacetValue>()?.let {
            val facetClass = it.facetClass
            val facetNameToQuery = facetClass.toFacetName()
            val facetValues = conceptNode.facetValues[facetNameToQuery] ?: throw IllegalStateException("Facet values not found for facet ${facetClass}.")

            val resultList = facetValues.map(::mapFacetValue)
            return@invoke MethodUtil.toMethodReturnType(method, resultList)
        }


        method.findAnnotation<QueryConceptIdentifierValue>()?.let {
            return@invoke when(method.returnType.kotlin) {
                String::class -> conceptNode.conceptIdentifier.name
                ConceptIdentifier::class -> conceptNode.conceptIdentifier
                else -> throw IllegalStateException("Unsupported type for conceptIdentifier method.")
            }
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method)
    }

    private fun mapFacetValue(facetValue: Any): Any {
        return if(facetValue is ConceptNode) {
            ProxyCreator.createProxy(
                definitionClass = facetValue.conceptName.clazz,
                invocationHandler = ConceptInstanceInvocationHandler(facetValue),
            )
        } else {
            facetValue
        }
    }
}
