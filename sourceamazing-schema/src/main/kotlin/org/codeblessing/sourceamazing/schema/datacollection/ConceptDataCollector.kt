package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.ConceptData
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier

class ConceptDataCollector {

    private var sequenceNumber: Int = 0;

    private val conceptData: MutableMap<ConceptIdentifier, ConceptData> = mutableMapOf()

    fun existingConceptData(conceptIdentifier: ConceptIdentifier): ConceptData {
        return conceptData[conceptIdentifier] ?: throw IllegalArgumentException("No concept with concept id '$conceptIdentifier' found.")
    }

    fun existingOrNewConceptData(conceptName: ConceptName, conceptIdentifier: ConceptIdentifier): ConceptData {
        return conceptData.getOrPut(conceptIdentifier) {
            ConceptDataImpl(sequenceNumber++, conceptName, conceptIdentifier)
        }
    }

    fun provideConceptData(): List<ConceptData> {
        return conceptData.values.toList()
    }
}
