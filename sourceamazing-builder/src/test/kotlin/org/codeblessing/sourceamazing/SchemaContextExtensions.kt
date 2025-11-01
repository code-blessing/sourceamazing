package org.codeblessing.sourceamazing

import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.SchemaContext

fun SchemaContext.toConceptName(conceptIdentifier: ConceptIdentifier): ConceptName {
    return dataCollector.existingConceptData(conceptIdentifier).conceptName
}
