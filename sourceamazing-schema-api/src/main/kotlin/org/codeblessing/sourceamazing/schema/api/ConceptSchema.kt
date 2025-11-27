package org.codeblessing.sourceamazing.schema.api

interface ConceptSchema {
    val conceptName: ConceptName
    val facets: List<FacetSchema>
    val facetNames: List<FacetName>

    fun hasFacet(facetName: FacetName): Boolean

    fun facetByName(facetName: FacetName): FacetSchema
}
