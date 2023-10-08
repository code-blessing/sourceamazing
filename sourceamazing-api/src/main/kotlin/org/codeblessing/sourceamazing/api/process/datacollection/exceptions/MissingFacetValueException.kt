package org.codeblessing.sourceamazing.api.process.datacollection.exceptions

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName


class MissingFacetValueException(
    concept: ConceptName,
    conceptIdentifier: ConceptIdentifier,
    facetName: FacetName,
): SchemaValidationException(
    "The entry with the identifier '${conceptIdentifier.name}' ('${concept.name}') " +
    "is missing a value for facet '${facetName.name}'. "
)
