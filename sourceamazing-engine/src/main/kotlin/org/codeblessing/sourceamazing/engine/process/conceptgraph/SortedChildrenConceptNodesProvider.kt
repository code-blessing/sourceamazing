package org.codeblessing.sourceamazing.engine.process.conceptgraph

import org.codeblessing.sourceamazing.api.process.schema.ConceptName

interface SortedChildrenConceptNodesProvider {
    fun children(conceptName: ConceptName): List<ConceptNode>
    fun children(conceptNames: Set<ConceptName>): List<ConceptNode>
}
