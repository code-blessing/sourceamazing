package org.codeblessing.sourceamazing.engine.process.conceptgraph

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName

class MutableConceptNode(
    override val sequenceNumber: Int,
    override val conceptName: ConceptName,
    override val conceptIdentifier: ConceptIdentifier,
    override var parentConceptNode: MutableConceptNode? = null,
    var childrenConceptNodes: Map<ConceptName, List<MutableConceptNode>> = emptyMap(),
    override var facetValues: MutableMap<FacetName, Any?> = mutableMapOf(),
): ConceptNode {
    override fun children(conceptName: ConceptName): List<ConceptNode> {
        return children(setOf(conceptName))
    }

    override fun children(conceptNames: Set<ConceptName>): List<ConceptNode> {
        return conceptNames.flatMap { childrenConceptNodes[it] ?: emptyList() }.sortedBy { it.sequenceNumber }
    }

}
