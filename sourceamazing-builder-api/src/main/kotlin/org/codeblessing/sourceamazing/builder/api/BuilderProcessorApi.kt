package org.codeblessing.sourceamazing.builder.api

import org.codeblessing.sourceamazing.schema.api.ConceptNameAndIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import kotlin.reflect.KClass

interface BuilderProcessorApi {

    fun <B : Any> withBuilder(
        schemaContext: SchemaContext,
        builderClass: KClass<B>,
        rootAliases: Map<String, ConceptNameAndIdentifier>,
        builderUsage: (builder: B) -> Unit,
    )
}
