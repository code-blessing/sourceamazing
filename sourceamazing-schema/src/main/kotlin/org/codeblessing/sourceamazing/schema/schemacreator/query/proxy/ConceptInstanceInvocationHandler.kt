package org.codeblessing.sourceamazing.schema.schemacreator.query.proxy

import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.conceptgraph.ConceptNode
import org.codeblessing.sourceamazing.schema.proxy.InvocationHandlerHelper
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.typemirror.MirrorFactory
import org.codeblessing.sourceamazing.schema.typemirror.QueryConceptIdentifierValueAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.QueryFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.schema.util.MethodUtil
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class ConceptInstanceInvocationHandler(private val conceptNode: ConceptNode): InvocationHandler  {

    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any? {
        val method = InvocationHandlerHelper.validateInvocationArguments(proxyOrNull, methodOrNull, argsOrNull)

        val methodMirror = MirrorFactory.convertToMethodMirror(method)

        methodMirror.getAnnotationMirrorOrNull(QueryFacetValueAnnotationMirror::class)?.let {
            val facetClass = it.facetClass.provideMirror()
            val facetNameToQuery = FacetName.of(facetClass)
            val facetValues = conceptNode.facetValues[facetNameToQuery] ?: throw IllegalStateException("Facet values not found for facet ${facetClass}.")

            val resultList = facetValues.map(::mapFacetValue)
            return MethodUtil.toMethodReturnType(method, resultList)
        }

        methodMirror.getAnnotationMirrorOrNull(QueryConceptIdentifierValueAnnotationMirror::class)?.let {
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
            ProxyCreator.createProxy(
                definitionClass = facetValue.conceptName.clazz.convertToKClass(),
                invocationHandler = ConceptInstanceInvocationHandler(facetValue),
            )
        } else {
            facetValue
        }
    }
}
