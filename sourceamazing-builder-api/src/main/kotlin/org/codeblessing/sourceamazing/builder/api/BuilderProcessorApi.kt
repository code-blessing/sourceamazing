package org.codeblessing.sourceamazing.builder.api

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.ConceptNameAndIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaContext

interface BuilderProcessorApi {

    fun <B : Any> withBuilder(
        schemaContext: SchemaContext,
        builderClass: KClass<B>,
        rootAliases: Map<String, ConceptNameAndIdentifier>,
        builderUsage: (builder: B) -> Unit,
    )
}
