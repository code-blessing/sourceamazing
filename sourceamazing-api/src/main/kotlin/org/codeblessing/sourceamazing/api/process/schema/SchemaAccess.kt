package org.codeblessing.sourceamazing.api.process.schema

interface SchemaAccess {
    fun conceptByConceptName(conceptName: ConceptName): ConceptSchema

    fun hasConceptName(conceptName: ConceptName): Boolean

    fun allConcepts(): Set<ConceptSchema>

    fun allRootConcepts(): Set<ConceptSchema>
    fun allChildrenConcepts(concept: ConceptSchema): Set<ConceptSchema>
}
