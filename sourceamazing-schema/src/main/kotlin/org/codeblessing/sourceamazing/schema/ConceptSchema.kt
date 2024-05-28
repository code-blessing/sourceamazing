package org.codeblessing.sourceamazing.schema


interface ConceptSchema {
    val conceptName: ConceptName
    val facets: List<FacetSchema>

    val facetNames: List<FacetName>
        get() = facets.map { it.facetName }.toList()

    fun hasFacet(facetName: FacetName): Boolean {
        return facetNames.contains(facetName)
    }

    fun facetByName(facetName: FacetName): FacetSchema {
        return facets.first { it.facetName == facetName }
    }
}
