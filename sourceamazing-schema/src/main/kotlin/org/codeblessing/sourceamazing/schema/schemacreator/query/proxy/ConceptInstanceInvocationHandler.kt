package org.codeblessing.sourceamazing.schema.schemacreator.query.proxy

import org.codeblessing.sourceamazing.schema.conceptgraph.ConceptNode
import org.codeblessing.sourceamazing.schema.proxy.KotlinInvocationHandler
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.schemacreator.query.QueryMethodUtil
import org.codeblessing.sourceamazing.schema.toFacetName
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

class ConceptInstanceInvocationHandler(private val conceptNode: ConceptNode): KotlinInvocationHandler(
    allowMemberProperties = true,
    allowMemberFunctions = false
)  {

    override fun invoke(proxy: Any, function: KFunction<*>, arguments: List<Any?>): Any? {
        if(function is KProperty.Getter<*>) {
            val property = function.property

            val facetName = property.name.toFacetName()
            val facetValues = requireNotNull(conceptNode.facetValues[facetName]) {
                "Facet values not found for facet $facetName and concept ${conceptNode.conceptName}."
            }

            val facetValueList = facetValues.map(::mapFacetValue)
            return QueryMethodUtil.adaptResultToType(property.returnType, facetValueList)
        }

        throw IllegalArgumentException("Method $function is not a supported property.")
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
