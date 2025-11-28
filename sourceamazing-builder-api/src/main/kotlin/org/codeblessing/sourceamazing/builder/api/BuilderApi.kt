package org.codeblessing.sourceamazing.builder.api

import java.util.*
import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.ConceptNameAndIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaContext

object BuilderApi {

    fun <B : Any> withBuilder(
        schemaContext: SchemaContext,
        builderClass: KClass<B>,
        builderUsage: (builder: B) -> Unit,
    ) {
        withBuilder(
            schemaContext = schemaContext,
            builderClass = builderClass,
            builderRootAliases = emptyMap(),
            builderUsage = builderUsage,
        )
    }

    fun <B : Any> withBuilder(
        schemaContext: SchemaContext,
        builderClass: KClass<B>,
        builderRootAliases: Map<String, ConceptNameAndIdentifier>,
        builderUsage: (builder: B) -> Unit,
    ) {
        val builderProcessorApis: ServiceLoader<BuilderProcessorApi> =
            ServiceLoader.load(BuilderProcessorApi::class.java)

        val builderProcessorApi =
            requireNotNull(builderProcessorApis.firstOrNull()) {
                "Could not find an implementation of the interface '${BuilderProcessorApi::class}'."
            }
        builderProcessorApi.withBuilder(schemaContext, builderClass, builderRootAliases, builderUsage)
    }
}
