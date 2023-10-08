package org.codeblessing.sourceamazing.api.process.conceptgraph.exceptions

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName


class ParentConceptNotFoundConceptGraphException(val conceptName: ConceptName, val conceptIdentifier: ConceptIdentifier, val parentConceptIdentifier: ConceptIdentifier): ConceptGraphException(
    "Concept with identifier '${conceptIdentifier.name}' (${conceptName.name}) could not found its parent concept '${parentConceptIdentifier.name}'."
)
