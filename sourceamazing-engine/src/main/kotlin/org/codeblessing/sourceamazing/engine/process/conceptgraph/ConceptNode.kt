package org.codeblessing.sourceamazing.engine.process.conceptgraph

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName

interface ConceptNode {
    val sequenceNumber: Int
    val conceptName: ConceptName
    val conceptIdentifier: ConceptIdentifier
    val facetValues: Map<FacetName, List<Any>> // every facet has at least an empty list!
}
