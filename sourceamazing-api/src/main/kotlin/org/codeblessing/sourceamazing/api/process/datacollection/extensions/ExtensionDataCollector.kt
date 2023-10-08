package org.codeblessing.sourceamazing.api.process.datacollection.extensions

import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier

interface ExtensionDataCollector {

    fun existingConceptData(
        conceptIdentifier: ConceptIdentifier
    ): ConceptData

    fun existingOrNewConceptData(
        conceptName: ConceptName,
        conceptIdentifier: ConceptIdentifier,
        parentConceptIdentifier: ConceptIdentifier? = null
    ): ConceptData

}
