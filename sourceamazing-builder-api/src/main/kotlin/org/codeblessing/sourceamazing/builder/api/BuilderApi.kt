package org.codeblessing.sourceamazing.builder.api

import org.codeblessing.sourceamazing.schema.api.SchemaContext
import java.util.*
import kotlin.reflect.KClass

object BuilderApi {

    fun <I : Any> withBuilder(schemaContext: SchemaContext, inputDefinitionClass: KClass<I>, builderUsage: (builder: I) -> Unit) {
        val builderProcessorApis: ServiceLoader<BuilderProcessorApi> = ServiceLoader.load(BuilderProcessorApi::class.java)

        val builderProcessorApi = requireNotNull(builderProcessorApis.firstOrNull()) {
            "Could not find an implementation of the interface '${BuilderProcessorApi::class}'."
        }
        builderProcessorApi.withBuilder(schemaContext, inputDefinitionClass, builderUsage)
    }
}