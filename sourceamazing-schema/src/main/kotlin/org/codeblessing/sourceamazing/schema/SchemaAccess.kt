package org.codeblessing.sourceamazing.schema

interface SchemaAccess {
    fun hasConceptName(conceptName: ConceptName): Boolean
    fun conceptByConceptName(conceptName: ConceptName): ConceptSchema
    fun allConcepts(): Set<ConceptSchema>
    fun facetByFacetName(facetName: FacetName): FacetSchema?
}

