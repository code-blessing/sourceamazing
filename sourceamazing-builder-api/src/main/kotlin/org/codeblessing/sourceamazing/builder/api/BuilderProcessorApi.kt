package org.codeblessing.sourceamazing.builder.api

import org.codeblessing.sourceamazing.schema.api.SchemaContext
import kotlin.reflect.KClass

interface BuilderProcessorApi {

    fun <I : Any> withBuilder(schemaContext: SchemaContext, inputDefinitionClass: KClass<I>, builderUsage: (builder: I) -> Unit)
}