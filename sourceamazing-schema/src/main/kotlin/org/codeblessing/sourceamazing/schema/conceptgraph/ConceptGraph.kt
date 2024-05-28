package org.codeblessing.sourceamazing.schema.conceptgraph

import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.ConceptName

class ConceptGraph(
    private val concepts: Map<ConceptIdentifier, ConceptNode>,
) {

    @Throws(NoSuchElementException::class)
    fun conceptByConceptIdentifier(conceptIdentifier: ConceptIdentifier): ConceptNode {
        return concepts[conceptIdentifier] ?: throw NoSuchElementException("No ConceptNode with id '${conceptIdentifier.name}'.")
    }

    fun conceptsByConceptNames(conceptNames: Set<ConceptName>): List<ConceptNode> {
        return concepts.values.filter { conceptNames.contains(it.conceptName) }
    }
}
