package org.codeblessing.sourceamazing.schema.api

interface ConceptDataCollector {
    fun existingConceptData(conceptIdentifier: ConceptIdentifier): ConceptData

    fun existingOrNewConceptData(conceptName: ConceptName, conceptIdentifier: ConceptIdentifier): ConceptData

    fun newConceptData(conceptName: ConceptName, conceptIdentifier: ConceptIdentifier): ConceptData

    fun newConceptData(conceptName: ConceptName): ConceptData

    fun validateAfterUpdate(conceptData: ConceptData)
}
