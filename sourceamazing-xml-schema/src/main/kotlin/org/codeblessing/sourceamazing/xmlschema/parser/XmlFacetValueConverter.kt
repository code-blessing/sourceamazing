package org.codeblessing.sourceamazing.xmlschema.parser

import org.codeblessing.sourceamazing.schema.FacetSchema
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.type.enumValues

object XmlFacetValueConverter {
    fun convertString(facetSchema: FacetSchema, attributeValue: String): Any {
        return when(facetSchema.facetType) {
            FacetType.TEXT -> attributeValue
            FacetType.NUMBER -> attributeValue.toInt()
            FacetType.BOOLEAN -> attributeValue.toBoolean()
            FacetType.REFERENCE -> ConceptIdentifier.of(attributeValue)
            FacetType.TEXT_ENUMERATION -> enumerationValue(facetSchema, attributeValue)
        }
    }

    private fun enumerationValue(facetSchema: FacetSchema, attributeValue: String): Any {
        val enumerationType = facetSchema.enumerationType
            ?: throw IllegalStateException("No enumeration type defined for facet ${facetSchema.facetName} but value was '$attributeValue'")
        return enumerationType.enumValues
            .firstOrNull { enumValue -> enumValue.name == attributeValue }
            ?: throw IllegalStateException("Value '$attributeValue' is not within the possible values ${enumerationType.enumValues.joinToString(",") { "'${it}'" }} for facet ${facetSchema.facetName}.")
    }
}

