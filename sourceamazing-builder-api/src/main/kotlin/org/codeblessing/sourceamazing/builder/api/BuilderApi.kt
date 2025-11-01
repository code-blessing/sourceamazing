package org.codeblessing.sourceamazing.builder.api

import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import java.util.*
import kotlin.reflect.KClass

object BuilderApi {

    fun <B : Any> withBuilder(
        schemaContext: SchemaContext,
        rootConceptName: ConceptName,
        rootConceptIdentifier: ConceptIdentifier,
        builderClass: KClass<B>,
        builderUsage: (builder: B) -> Unit
    ) {
        val builderProcessorApis: ServiceLoader<BuilderProcessorApi> = ServiceLoader.load(BuilderProcessorApi::class.java)

        val builderProcessorApi = requireNotNull(builderProcessorApis.firstOrNull()) {
            "Could not find an implementation of the interface '${BuilderProcessorApi::class}'."
        }
        builderProcessorApi.withBuilder(schemaContext, rootConceptName, rootConceptIdentifier, builderClass, builderUsage)
    }
}
