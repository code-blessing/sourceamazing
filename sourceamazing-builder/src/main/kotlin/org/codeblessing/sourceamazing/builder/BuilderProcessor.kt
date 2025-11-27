package org.codeblessing.sourceamazing.builder

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.BuilderProcessorApi
import org.codeblessing.sourceamazing.builder.proxy.BuilderInvocationHandler
import org.codeblessing.sourceamazing.builder.validation.BuilderHierarchyValidator
import org.codeblessing.sourceamazing.schema.api.ConceptNameAndIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.utils.proxy.ProxyCreator

class BuilderProcessor() : BuilderProcessorApi {

    override fun <I : Any> withBuilder(
        schemaContext: SchemaContext,
        builderClass: KClass<I>,
        rootAliases: Map<String, ConceptNameAndIdentifier>,
        builderUsage: (builder: I) -> Unit,
    ) {
        val schemaAccess = schemaContext.schema
        val rootAliasesTyped = rootAliases.mapKeys { (key) -> key.toAlias() }

        BuilderHierarchyValidator.validateTopLevelBuilderMethods(
            builderClass,
            schemaAccess,
            rootAliasesTyped.mapValues { it.value.conceptName },
        )
        val builderImplementation: I =
            ProxyCreator.createProxy(
                builderClass,
                BuilderInvocationHandler(
                    schemaAccess = schemaAccess,
                    builderClass = builderClass,
                    conceptDataCollector = schemaContext.dataCollector,
                    superiorAliases = rootAliasesTyped,
                ),
            )
        builderUsage(builderImplementation)
    }
}
