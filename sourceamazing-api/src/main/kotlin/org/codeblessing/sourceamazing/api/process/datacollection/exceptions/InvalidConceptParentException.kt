package org.codeblessing.sourceamazing.api.process.datacollection.exceptions

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName


class InvalidConceptParentException(val concept: ConceptName,
                                    val conceptIdentifier: ConceptIdentifier,
                                    val parentConceptIdentifier: ConceptIdentifier?,
    ): SchemaValidationException(
    "The entry with the identifier '${conceptIdentifier.name}' ('${concept.name}') has an invalid parent concept identifier '${parentConceptIdentifier?.name}'."
)
