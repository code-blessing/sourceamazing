package org.codeblessing.sourceamazing.xmlschema.parser

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.FacetSchema
import org.codeblessing.sourceamazing.api.process.schema.FacetTypeEnum

object XmlFacetValueConverter {
    fun convertString(facetSchema: FacetSchema, attributeValue: String): Any? {
        return when(facetSchema.facetType) {
            FacetTypeEnum.TEXT -> attributeValue
            FacetTypeEnum.NUMBER -> attributeValue.toLong()
            FacetTypeEnum.BOOLEAN -> attributeValue.toBoolean()
            FacetTypeEnum.REFERENCE -> ConceptIdentifier.of(attributeValue)
            FacetTypeEnum.TEXT_ENUMERATION -> enumerationValue(facetSchema, attributeValue)
        }
    }

    private fun enumerationValue(facetSchema: FacetSchema, attributeValue: String): Any {
        val enumerationType = facetSchema.enumerationType
            ?: throw IllegalStateException("No enumeration type defined for facet ${facetSchema.facetName} but value was '$attributeValue'")
        return enumerationType.java.enumConstants
            .filterIsInstance(Enum::class.java)
            .firstOrNull { enumConstant ->
                return@firstOrNull enumConstant.name == attributeValue
            }
            ?: throw IllegalStateException("Value '$attributeValue' is not within the possible values ${enumerationType.java.enumConstants.joinToString(",") { "'${it}'" }} for facet ${facetSchema.facetName}.")
    }
}

