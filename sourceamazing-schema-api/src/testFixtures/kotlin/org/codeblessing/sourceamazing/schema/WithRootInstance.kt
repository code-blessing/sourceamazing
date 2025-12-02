package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.api.datacollection.ConceptData
import org.codeblessing.sourceamazing.schema.api.schemaaccess.BooleanFacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.ConceptSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.EnumFacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.NumberFacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.ReferenceFacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.TextFacetSchema

inline fun <reified T : Any> SchemaContext.withDefaultValueRootInstance(
    executable: (rootConceptAndId: ConceptData) -> Unit
): ConceptIdentifier {
    val rootConceptClass = T::class
    val rootConceptName = rootConceptClass.toConceptName()
    val rootConcept = schema.allConcepts().first { it.conceptName == rootConceptName }
    val rootConceptData = dataCollector.newConceptData(rootConceptName)
    writeDefaultValues(rootConcept, rootConceptData)

    val rootConceptAndId =
        ConceptNameAndIdentifier(
            conceptName = rootConceptData.conceptName,
            conceptIdentifier = rootConceptData.conceptIdentifier,
        )
    executable(rootConceptData)
    return rootConceptAndId.conceptIdentifier
}

fun writeDefaultValues(rootConcept: ConceptSchema, rootConceptData: ConceptData) {
    rootConcept.facets.forEach { facetSchema ->
        if (facetSchema.minimumOccurrences > 0) {
            val defaultValue =
                when (facetSchema) {
                    is TextFacetSchema -> ""
                    is NumberFacetSchema -> 0
                    is BooleanFacetSchema -> false
                    is EnumFacetSchema -> facetSchema.enumerationValues.firstOrNull()
                    is ReferenceFacetSchema -> null
                }
            if (defaultValue != null) {
                rootConceptData.addFacetValue(facetSchema.facetName, defaultValue)
            }
        }
    }
}

inline fun <reified T> SchemaContext.withRootInstance(block: (ConceptData) -> Unit): ConceptIdentifier {
    val rootConcept = dataCollector.newConceptData(T::class.toConceptName())
    block(rootConcept)
    return rootConcept.conceptIdentifier
}
