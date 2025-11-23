package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.*

inline fun <reified T : Any> withRootInstance(
    schemaContext: SchemaContext,
    executable: (rootConceptAndId: ConceptNameAndIdentifier) -> Unit,
): ConceptIdentifier {
    val rootConceptClass = T::class
    val rootConceptName = rootConceptClass.toConceptName()
    val rootConcept = schemaContext.schema.allConcepts().first { it.conceptName == rootConceptName }
    val rootConceptData = schemaContext.dataCollector.newConceptData(rootConceptName)
    writeDefaultValues(rootConcept, rootConceptData)

    val rootConceptAndId =
        ConceptNameAndIdentifier(
            conceptName = rootConceptData.conceptName,
            conceptIdentifier = rootConceptData.conceptIdentifier,
        )
    executable(rootConceptAndId)
    return rootConceptAndId.conceptIdentifier
}

fun writeDefaultValues(rootConcept: ConceptSchema, rootConceptData: ConceptData) {
    rootConcept.facets.forEach { facetSchema ->
        if (facetSchema.minimumOccurrences > 0) {
            val defaultValue =
                when (facetSchema.facetType) {
                    FacetType.TEXT -> ""
                    FacetType.NUMBER -> 0
                    FacetType.BOOLEAN -> false
                    FacetType.TEXT_ENUMERATION -> facetSchema.enumerationValues.firstOrNull()
                    FacetType.REFERENCE -> null
                }
            if (defaultValue != null) {
                rootConceptData.addFacetValue(facetSchema.facetName, defaultValue)
            }
        }
    }
}
