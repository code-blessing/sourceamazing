package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaContext

inline fun <reified T : Any> withRootInstance(
    schemaContext: SchemaContext,
    executable: (rootConceptId: ConceptIdentifier) -> Unit
): ConceptIdentifier {
    val rootConceptClass = T::class
    val rootConceptName = rootConceptClass.toConceptName()
    val rootConcept = schemaContext.schema.allConcepts().first { it.conceptName == rootConceptName }
    val rootConceptData = schemaContext.dataCollector.newConceptData(rootConceptName)
    rootConcept.facets.forEach { rootFacetSchema ->
        // TODO fill all facets with default values
    }

    executable(rootConceptData.conceptIdentifier)
    return rootConceptData.conceptIdentifier
}
