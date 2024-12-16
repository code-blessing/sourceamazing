package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderProcessorApi
import org.codeblessing.sourceamazing.builder.proxy.BuilderInvocationHandler
import org.codeblessing.sourceamazing.builder.validation.BuilderHierarchyValidator
import org.codeblessing.sourceamazing.schema.SchemaContextAccessor.toRevealedSchemaContext
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import kotlin.reflect.KClass

class BuilderProcessor(): BuilderProcessorApi {

    override fun <I : Any> withBuilder(schemaContext: SchemaContext, builderClass: KClass<I>, builderUsage: (builder: I) -> Unit) {
        val schemaAccess = schemaContext.toRevealedSchemaContext().schema
        BuilderHierarchyValidator.validateTopLevelBuilderMethods(builderClass, schemaAccess)
        val schemaContextImplementation = schemaContext.toRevealedSchemaContext()
        val builderImplementation: I = ProxyCreator.createProxy(builderClass, BuilderInvocationHandler(
            schemaAccess = schemaAccess,
            builderClass = builderClass,
            conceptDataCollector = schemaContextImplementation.conceptDataCollector,
            superiorConcepts = emptyMap(),
            superiorConceptIds = emptyMap()
        ))
        builderUsage(builderImplementation)
    }
}