package org.codeblessing.sourceamazing.schema.api.schemaaccess

import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.FacetName

interface ConceptSchema {
    val conceptName: ConceptName
    val facets: List<FacetSchema>
    val facetNames: List<FacetName>

    fun hasFacet(facetName: FacetName): Boolean

    fun facetByName(facetName: FacetName): FacetSchema
}
