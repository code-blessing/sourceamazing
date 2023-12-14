package org.codeblessing.sourceamazing.engine.process.datacollection.validation.exceptions

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName


class WrongCardinalityForFacetValueException(val concept: ConceptName, val conceptIdentifier: ConceptIdentifier, facetName: FacetName, reason: String): SchemaValidationException(
    "Facet '$facetName' for concept identifier '${conceptIdentifier.name}' in concept '${concept}' has a wrong cardinality: $reason"
)
