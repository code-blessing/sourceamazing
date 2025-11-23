package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.datacollection.validation.ConceptDataValidator

class ConceptDataCollectorImpl(private val schemaAccess: SchemaAccess) : ConceptDataCollector {

    private var sequenceNumber: Int = 0

    private val conceptData: MutableMap<ConceptIdentifier, ConceptData> = mutableMapOf()

    override fun existingConceptData(conceptIdentifier: ConceptIdentifier): ConceptData {
        return conceptData[conceptIdentifier]
            ?: throw IllegalArgumentException(
                "No concept with concept id '$conceptIdentifier' found."
            )
    }

    override fun existingOrNewConceptData(
        conceptName: ConceptName,
        conceptIdentifier: ConceptIdentifier,
    ): ConceptData {
        return conceptData.getOrPut(conceptIdentifier) {
            createNewConceptData(conceptName, conceptIdentifier)
        }
    }

    override fun newConceptData(conceptName: ConceptName): ConceptData {
        return newConceptData(conceptName, conceptName.randomConceptIdentifier())
    }

    override fun newConceptData(
        conceptName: ConceptName,
        conceptIdentifier: ConceptIdentifier,
    ): ConceptData {
        val newConceptData = createNewConceptData(conceptName, conceptIdentifier)
        ConceptDataValidator.validateDuplicateConceptIdentifiers(conceptData.keys, newConceptData)
        conceptData[conceptIdentifier] = newConceptData
        return newConceptData
    }

    override fun validateAfterUpdate(conceptData: ConceptData) {
        ConceptDataValidator.validateEntryWithoutReferenceAndCardinalityIntegrity(
            schemaAccess,
            conceptData,
        )
    }

    private fun createNewConceptData(
        conceptName: ConceptName,
        conceptIdentifier: ConceptIdentifier,
    ): ConceptData {
        return ConceptDataImpl(sequenceNumber++, conceptName, conceptIdentifier)
    }

    fun provideConceptData(): List<ConceptData> {
        return conceptData.values.toList()
    }
}
