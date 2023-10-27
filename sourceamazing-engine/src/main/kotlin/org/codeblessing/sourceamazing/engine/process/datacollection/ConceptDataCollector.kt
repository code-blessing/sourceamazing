package org.codeblessing.sourceamazing.engine.process.datacollection

import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.ExtensionDataCollector
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName

class ConceptDataCollector: ExtensionDataCollector {

    private var sequenceNumber: Int = 0;

    private val conceptData: MutableMap<ConceptIdentifier, ConceptData> = mutableMapOf()

    override fun existingConceptData(conceptIdentifier: ConceptIdentifier): ConceptData {
        return conceptData[conceptIdentifier] ?: throw IllegalArgumentException("No concept with concept id '$conceptIdentifier' found.")
    }

    override fun existingOrNewConceptData(conceptName: ConceptName, conceptIdentifier: ConceptIdentifier, parentConceptIdentifier: ConceptIdentifier?): ConceptData {
        return conceptData.getOrPut(conceptIdentifier) {
            ConceptDataImpl(sequenceNumber++, conceptName, conceptIdentifier)
        }.setParentConceptIdentifier(parentConceptIdentifier)
    }

    fun provideConceptData(): List<ConceptData> {
        return conceptData.values.toList()
    }
}
