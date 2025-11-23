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
        val superiorConcepts =
            rootAliasesTyped.mapValues { (_, value) -> value.conceptName }.toMap()
        val superiorConceptIds =
            rootAliasesTyped.mapValues { (_, value) -> value.conceptIdentifier }.toMap()

        BuilderHierarchyValidator.validateTopLevelBuilderMethods(
            builderClass,
            schemaAccess,
            superiorConcepts,
        )
        val builderImplementation: I =
            ProxyCreator.createProxy(
                builderClass,
                BuilderInvocationHandler(
                    schemaAccess = schemaAccess,
                    builderClass = builderClass,
                    conceptDataCollector = schemaContext.dataCollector,
                    superiorConcepts = superiorConcepts,
                    superiorConceptIds = superiorConceptIds,
                ),
            )
        builderUsage(builderImplementation)
    }
}
