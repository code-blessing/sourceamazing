package org.codeblessing.sourceamazing.builder.api

import org.codeblessing.sourceamazing.schema.api.SchemaContext
import kotlin.reflect.KClass

interface BuilderProcessorApi {

    fun <B : Any> withBuilder(schemaContext: SchemaContext, builderClass: KClass<B>, builderUsage: (builder: B) -> Unit)
}