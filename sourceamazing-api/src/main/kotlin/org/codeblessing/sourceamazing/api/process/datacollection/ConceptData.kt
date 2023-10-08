package org.codeblessing.sourceamazing.api.process.datacollection

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier

interface ConceptData {
    val sequenceNumber: Int
    val conceptName: ConceptName
    val conceptIdentifier: ConceptIdentifier

    val parentConceptIdentifier: ConceptIdentifier?
    fun setParentConceptIdentifier(parentConceptIdentifier: ConceptIdentifier?): ConceptData

    fun allFacets(): Map<FacetName, Any?>
    fun hasFacet(facetName: FacetName):Boolean
    fun getFacet(facetName: FacetName):Any?
    fun getFacetNames(): Set<FacetName>

    fun addOrReplaceFacetValues(facetValues: Map<FacetName, Any?>): ConceptData
    fun addOrReplaceFacetValue(facetName: FacetName, facetValue: Any?): ConceptData

}
