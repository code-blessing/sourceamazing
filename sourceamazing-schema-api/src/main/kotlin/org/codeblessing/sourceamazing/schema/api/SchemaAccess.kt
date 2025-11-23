package org.codeblessing.sourceamazing.schema.api

interface SchemaAccess {
    fun hasConceptName(
        conceptName: ConceptName
    ): Boolean // TODO Remove and make conceptByConceptName nullable

    fun conceptByConceptName(conceptName: ConceptName): ConceptSchema

    fun allConcepts(): Set<ConceptSchema>

    fun facetByFacetName(facetName: FacetName): FacetSchema? // TODO This makes no sense anymore
    // TODO Add rootConceptName(): ConceptSchema method
}
