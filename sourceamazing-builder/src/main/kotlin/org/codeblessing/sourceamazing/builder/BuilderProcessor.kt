package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderProcessorApi
import org.codeblessing.sourceamazing.builder.proxy.BuilderInvocationHandler
import org.codeblessing.sourceamazing.builder.validation.BuilderHierarchyValidator
import org.codeblessing.sourceamazing.schema.SchemaContextAccessor.toRevealedSchemaContext
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import kotlin.reflect.KClass

class BuilderProcessor(): BuilderProcessorApi {

    override fun <I : Any> withBuilder(
        schemaContext: SchemaContext,
        rootConceptName: ConceptName,
        rootConceptIdentifier: ConceptIdentifier,
        builderClass: KClass<I>,
        builderUsage: (builder: I) -> Unit
    ) {
        val schemaAccess = schemaContext.toRevealedSchemaContext().schema
        val rootAlias = BuilderHierarchyValidator.validateTopLevelBuilderMethods(builderClass, schemaAccess, rootConceptName)
        val schemaContextImplementation = schemaContext.toRevealedSchemaContext()
        val builderImplementation: I = ProxyCreator.createProxy(builderClass, BuilderInvocationHandler(
            schemaAccess = schemaAccess,
            builderClass = builderClass,
            isTopLevelBuilder = true,
            conceptDataCollector = schemaContextImplementation.dataCollector,
            superiorConcepts = mapOf(rootAlias to rootConceptName),
            superiorConceptIds = mapOf(rootAlias to rootConceptIdentifier)
        ))
        builderUsage(builderImplementation)
    }
}
