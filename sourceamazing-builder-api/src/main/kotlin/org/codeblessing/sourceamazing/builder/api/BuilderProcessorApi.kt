package org.codeblessing.sourceamazing.builder.api

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.ClazzAndModelId
import org.codeblessing.sourceamazing.schema.api.SchemaContext

interface BuilderProcessorApi {

    fun <B : Any> withBuilder(
        schemaContext: SchemaContext,
        builderClass: KClass<B>,
        builderFactories: Set<BuilderFactory<*, *>>,
        rootAliases: Map<String, ClazzAndModelId>,
        builderUsage: (builder: B) -> Unit,
    )
}
