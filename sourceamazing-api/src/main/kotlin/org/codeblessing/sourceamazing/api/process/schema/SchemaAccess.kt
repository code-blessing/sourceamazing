package org.codeblessing.sourceamazing.api.process.schema

interface SchemaAccess {
    fun hasConceptName(conceptName: ConceptName): Boolean
    fun conceptByConceptName(conceptName: ConceptName): ConceptSchema
    fun allConcepts(): Set<ConceptSchema>
}

