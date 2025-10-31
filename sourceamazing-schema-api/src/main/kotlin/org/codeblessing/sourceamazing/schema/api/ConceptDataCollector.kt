package org.codeblessing.sourceamazing.schema.api

// TODO Maybe split reading and mutating parts in ConceptDataCollector and ConceptData
interface ConceptDataCollector {
    fun existingConceptData(conceptIdentifier: ConceptIdentifier): ConceptData

    fun existingOrNewConceptData(conceptName: ConceptName, conceptIdentifier: ConceptIdentifier): ConceptData

    fun newConceptData(conceptName: ConceptName, conceptIdentifier: ConceptIdentifier): ConceptData

    fun newConceptData(conceptName: ConceptName): ConceptData

    fun validateAfterUpdate(conceptData: ConceptData)

}
