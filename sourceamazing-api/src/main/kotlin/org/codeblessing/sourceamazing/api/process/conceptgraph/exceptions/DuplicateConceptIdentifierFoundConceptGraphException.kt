package org.codeblessing.sourceamazing.api.process.conceptgraph.exceptions

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier


class DuplicateConceptIdentifierFoundConceptGraphException(val concept: ConceptName, val conceptIdentifier: ConceptIdentifier): ConceptGraphException(
    "Duplicate concept identifier '${conceptIdentifier.name}' in concept '${concept.name}'."
)
