package org.codeblessing.sourceamazing.api.process.datacollection.extensions

import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName

interface ExtensionDataCollector {

    fun existingConceptData(
        conceptIdentifier: ConceptIdentifier
    ): ConceptData

    fun existingOrNewConceptData(
        conceptName: ConceptName,
        conceptIdentifier: ConceptIdentifier,
    ): ConceptData

}
