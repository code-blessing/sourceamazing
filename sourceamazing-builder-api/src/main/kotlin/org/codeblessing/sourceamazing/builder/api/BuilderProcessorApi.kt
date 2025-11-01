package org.codeblessing.sourceamazing.builder.api

import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import kotlin.reflect.KClass

interface BuilderProcessorApi {

    fun <B : Any> withBuilder(
        schemaContext: SchemaContext,
        rootConceptName: ConceptName,
        rootConceptIdentifier: ConceptIdentifier,
        builderClass: KClass<B>,
        builderUsage: (builder: B) -> Unit
    )
}
