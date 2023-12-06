package org.codeblessing.sourceamazing.engine.process.datacollection.validation.exceptions

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName


class DuplicateConceptIdentifierException(val concept: ConceptName, val conceptIdentifier: ConceptIdentifier): SchemaValidationException(
    "The entry with the identifier '${conceptIdentifier.name}' (concept: '${concept}') " +
            "occurred multiple times. A concept identifier must be unique."
)
