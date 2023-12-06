package org.codeblessing.sourceamazing.engine.process.conceptgraph

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName

class MutableConceptNode(
    override val sequenceNumber: Int,
    override val conceptName: ConceptName,
    override val conceptIdentifier: ConceptIdentifier,
    override var facetValues: MutableMap<FacetName, List<Any>> = mutableMapOf(),
): ConceptNode
