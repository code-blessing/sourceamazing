package org.codeblessing.sourceamazing.xmlschema.parser

import org.codeblessing.sourceamazing.schema.api.BooleanFacetSchema
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.EnumFacetSchema
import org.codeblessing.sourceamazing.schema.api.FacetSchema
import org.codeblessing.sourceamazing.schema.api.NumberFacetSchema
import org.codeblessing.sourceamazing.schema.api.ReferenceFacetSchema
import org.codeblessing.sourceamazing.schema.api.TextFacetSchema
import org.codeblessing.sourceamazing.utils.type.enumValues

object XmlFacetValueConverter {
    fun convertString(facetSchema: FacetSchema, attributeValue: String): Any {
        return when (facetSchema) {
            is TextFacetSchema -> attributeValue
            is NumberFacetSchema -> attributeValue.toInt()
            is BooleanFacetSchema -> attributeValue.toBoolean()
            is ReferenceFacetSchema -> ConceptIdentifier.of(attributeValue)
            is EnumFacetSchema -> enumerationValue(facetSchema, attributeValue)
        }
    }

    private fun enumerationValue(facetSchema: EnumFacetSchema, attributeValue: String): Any {
        val enumerationType =
            facetSchema.enumerationType
                ?: throw IllegalStateException(
                    "No enumeration type defined for facet ${facetSchema.facetName} but value was '$attributeValue'"
                )
        return enumerationType.enumValues.firstOrNull { enumValue ->
            enumValue.name == attributeValue
        }
            ?: throw IllegalStateException(
                "Value '$attributeValue' is not within the possible values ${enumerationType.enumValues.joinToString(",") { "'${it}'" }} for facet ${facetSchema.facetName}."
            )
    }
}
