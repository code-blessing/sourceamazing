package org.codeblessing.sourceamazing.schema.schemacreator.query.proxy

import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.conceptgraph.ConceptNode
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.proxy.KotlinInvocationHandler
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.schemacreator.query.QueryMethodUtil
import org.codeblessing.sourceamazing.schema.toFacetName
import org.codeblessing.sourceamazing.schema.type.KTypeUtil
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

class ConceptInstanceInvocationHandler(private val conceptNode: ConceptNode): KotlinInvocationHandler()  {

    override fun invoke(proxy: Any, function: KFunction<*>, arguments: List<Any?>): Any? {
        function.findAnnotation<QueryFacetValue>()?.let {
            val facetClass = it.facetClass
            val facetNameToQuery = facetClass.toFacetName()
            val facetValues = requireNotNull(conceptNode.facetValues[facetNameToQuery]) {
                "Facet values not found for facet ${facetClass}."
            }

            val facetValueList = facetValues.map(::mapFacetValue)

            return@invoke QueryMethodUtil.adaptResultToFunctionReturnType(function, facetValueList)
        }

        function.findAnnotation<QueryConceptIdentifierValue>()?.let {
            return@invoke when(KTypeUtil.classFromType(function.returnType)) {
                String::class -> conceptNode.conceptIdentifier.name
                ConceptIdentifier::class -> conceptNode.conceptIdentifier
                else -> throw IllegalStateException("Unsupported type ${function.returnType} for conceptIdentifier method.")
            }
        }

        throw IllegalArgumentException("Method $function has not the supported annotations (${QueryFacetValue::class.shortText()} or ${QueryConceptIdentifierValue::class.shortText()}).")
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
