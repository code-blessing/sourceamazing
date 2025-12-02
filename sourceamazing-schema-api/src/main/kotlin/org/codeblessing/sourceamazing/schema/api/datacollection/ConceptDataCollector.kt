package org.codeblessing.sourceamazing.schema.api.datacollection

import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.ConceptName

interface ConceptDataCollector {
    fun existingConceptData(conceptIdentifier: ConceptIdentifier): ConceptData

    fun existingOrNewConceptData(conceptName: ConceptName, conceptIdentifier: ConceptIdentifier): ConceptData

    fun newConceptData(conceptName: ConceptName, conceptIdentifier: ConceptIdentifier): ConceptData

    fun newConceptData(conceptName: ConceptName): ConceptData

    fun validateAfterUpdate(conceptData: ConceptData)
}
