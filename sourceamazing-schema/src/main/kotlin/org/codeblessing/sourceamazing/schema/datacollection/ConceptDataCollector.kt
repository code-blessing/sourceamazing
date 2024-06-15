package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.ConceptData
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.datacollection.validation.ConceptDataValidator

class ConceptDataCollector {

    private var sequenceNumber: Int = 0

    private val conceptData: MutableMap<ConceptIdentifier, ConceptData> = mutableMapOf()

    fun existingConceptData(conceptIdentifier: ConceptIdentifier): ConceptData {
        return conceptData[conceptIdentifier] ?: throw IllegalArgumentException("No concept with concept id '$conceptIdentifier' found.")
    }

    fun existingOrNewConceptData(conceptName: ConceptName, conceptIdentifier: ConceptIdentifier): ConceptData {
        return conceptData.getOrPut(conceptIdentifier) {
            createNewConceptData(conceptName, conceptIdentifier)
        }
    }

    fun newConceptData(conceptName: ConceptName, conceptIdentifier: ConceptIdentifier): ConceptData {
        val newConceptData = createNewConceptData(conceptName, conceptIdentifier)
        ConceptDataValidator.validateDuplicateConceptIdentifiers(conceptData.keys, newConceptData)
        conceptData[conceptIdentifier] = newConceptData
        return newConceptData
    }

    private fun createNewConceptData(conceptName: ConceptName, conceptIdentifier: ConceptIdentifier): ConceptData {
        return ConceptDataImpl(sequenceNumber++, conceptName, conceptIdentifier)
    }

    fun provideConceptData(): List<ConceptData> {
        return conceptData.values.toList()
    }
}
