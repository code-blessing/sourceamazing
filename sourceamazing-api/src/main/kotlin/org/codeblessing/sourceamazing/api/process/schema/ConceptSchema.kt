package org.codeblessing.sourceamazing.api.process.schema


interface ConceptSchema {
    val conceptName: ConceptName
    val conceptClass: Class<*>
    val parentConceptName: ConceptName?
    val facets: List<FacetSchema>
    val minOccurrence: Int
    val maxOccurrence: Int

    val facetNames: List<FacetName>
        get() = facets.map { it.facetName }.toList()

    fun hasFacet(facetName: FacetName): Boolean {
        return facetNames.contains(facetName)
    }

    fun facetByName(facetName: FacetName): FacetSchema {
        return facets.first { it.facetName == facetName }
    }
}
