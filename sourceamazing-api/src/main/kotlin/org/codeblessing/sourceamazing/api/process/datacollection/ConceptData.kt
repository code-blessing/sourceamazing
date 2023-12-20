package org.codeblessing.sourceamazing.api.process.datacollection

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName

interface ConceptData {
    val sequenceNumber: Int
    val conceptName: ConceptName
    val conceptIdentifier: ConceptIdentifier

    fun allFacets(): Map<FacetName, List<Any>>
    fun hasFacet(facetName: FacetName):Boolean
    fun getFacet(facetName: FacetName):List<Any>
    fun getFacetNames(): Set<FacetName>

    fun replaceFacetValues(facetName: FacetName, facetValues: List<Any>): ConceptData
    fun addFacetValue(facetName: FacetName, facetValue: Any): ConceptData
    fun addFacetValues(facetName: FacetName, facetValues: List<Any>): ConceptData

    fun describe(): String

}
