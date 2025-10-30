package org.codeblessing.sourceamazing.schema.api

interface SchemaContext {
    val schema: SchemaAccess
    val conceptDataCollector: ConceptDataCollector
}
