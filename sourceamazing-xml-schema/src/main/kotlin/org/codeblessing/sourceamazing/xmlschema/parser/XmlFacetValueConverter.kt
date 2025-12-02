package org.codeblessing.sourceamazing.xmlschema.parser

import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.schemaaccess.BooleanFacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.EnumFacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.FacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.NumberFacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.ReferenceFacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.TextFacetSchema
import org.codeblessing.sourceamazing.utils.enumeration.EnumUtil
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
        val enumerationClass = facetSchema.enumerationClass
        return EnumUtil.fromStringToEnum(attributeValue, enumerationClass)
            ?: throw IllegalStateException(
                "Value '$attributeValue' is not within the possible values ${enumerationClass.enumValues.joinToString(",") { "'${it}'" }} for facet ${facetSchema.facetName}."
            )
    }
}
