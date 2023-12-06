package org.codeblessing.sourceamazing.engine.process.datacollection.validation.exceptions

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName


class UnknownFacetNameException(val concept: ConceptName, val conceptIdentifier: ConceptIdentifier, facetName: FacetName, reason: String,): SchemaValidationException(
    "Unknown facet name '${facetName}' found for concept identifier '${conceptIdentifier.name}' in concept '${concept}': $reason."
)
