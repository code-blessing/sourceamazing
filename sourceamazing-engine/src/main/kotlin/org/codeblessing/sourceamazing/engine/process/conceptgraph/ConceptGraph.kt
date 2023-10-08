package org.codeblessing.sourceamazing.engine.process.conceptgraph

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import kotlin.jvm.Throws

class ConceptGraph(
    private val concepts: Map<ConceptIdentifier, ConceptNode>,
): SortedChildrenConceptNodesProvider {
    private val rootConceptsByConceptName: Map<ConceptName, List<ConceptNode>> = concepts.values
        .filter { it.parentConceptNode == null }
        .groupBy { it.conceptName }

    @Throws(NoSuchElementException::class)
    fun conceptByConceptIdentifier(conceptIdentifier: ConceptIdentifier): ConceptNode {
        return concepts[conceptIdentifier] ?: throw NoSuchElementException("No ConceptNode with id '${conceptIdentifier.name}'.")
    }

    fun childConcepts(conceptName: ConceptName, parentConceptIdentifier: ConceptIdentifier): List<ConceptNode> {
        return concepts.values
            .filter { conceptNode -> conceptNode.conceptName == conceptName }
            .filter { conceptNode -> conceptNode.parentConceptNode?.conceptIdentifier == parentConceptIdentifier }
            .sortedBy { conceptNode -> conceptNode.sequenceNumber }
    }

    fun rootConcepts(): Set<ConceptNode> {
        return concepts.values
            .filter { conceptNode -> conceptNode.parentConceptNode == null }
            .toSet()
    }

    override fun children(conceptName: ConceptName): List<ConceptNode> {
        return children(setOf(conceptName))
    }

    override fun children(conceptNames: Set<ConceptName>): List<ConceptNode> {
        return conceptNames
            .flatMap { conceptName -> rootConceptsByConceptName(conceptName) }
            .sortedBy { conceptNode -> conceptNode.sequenceNumber }
    }

    private fun rootConceptsByConceptName(conceptName: ConceptName): List<ConceptNode> {
        return rootConceptsByConceptName[conceptName]
            ?.sortedBy { conceptNode -> conceptNode.sequenceNumber }
            ?: emptyList()
    }

}
