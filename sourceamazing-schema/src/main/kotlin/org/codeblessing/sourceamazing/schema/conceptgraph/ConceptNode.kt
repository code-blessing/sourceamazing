package org.codeblessing.sourceamazing.schema.conceptgraph

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier

interface ConceptNode {
    val sequenceNumber: Int
    val conceptName: ConceptName
    val conceptIdentifier: ConceptIdentifier
    val facetValues: Map<FacetName, List<Any>> // every facet has at least an empty list!
}
