package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.ConceptData
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.api.toConceptName

// TODO merge with other default function
inline fun <reified T> SchemaContext.workOnRootInstance(block: (ConceptData) -> Unit): ConceptIdentifier {
    val rootConcept = dataCollector.newConceptData(T::class.toConceptName())
    block(rootConcept)
    return rootConcept.conceptIdentifier
}
