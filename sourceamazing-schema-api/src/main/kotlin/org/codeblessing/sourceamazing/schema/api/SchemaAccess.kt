package org.codeblessing.sourceamazing.schema.api

interface SchemaAccess {
    fun hasConceptName(conceptName: ConceptName): Boolean // TODO Remove and make conceptByConceptName nullable

    fun conceptByConceptName(conceptName: ConceptName): ConceptSchema

    fun allConcepts(): Set<ConceptSchema>

    fun facetByFacetName(conceptName: ConceptName, facetName: FacetName): FacetSchema?

    fun rootConceptName(): ConceptName
}
