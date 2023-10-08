package org.codeblessing.sourceamazing.api.process.conceptgraph.exceptions

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName

class ReferencedConceptConceptGraphNodeNotFoundException(
    val conceptIdentifier: ConceptIdentifier,
    conceptName: ConceptName,
    facetName: FacetName,
    referencedConceptIdentifier: ConceptIdentifier
): ConceptGraphException(
    "Concept with identifier '${conceptIdentifier.name}' (${conceptName.name}) has a facet '${facetName.name}' referencing a concept '${referencedConceptIdentifier.name}' that could not be found."
)
