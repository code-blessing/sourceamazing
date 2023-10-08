package org.codeblessing.sourceamazing.api.process.datacollection.exceptions

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName


class UnknownConceptException(val concept: ConceptName, val conceptIdentifier: ConceptIdentifier): SchemaValidationException(
    "The entry with the identifier '${conceptIdentifier.name}' points to a concept '${concept.name}' that is not known."
)
