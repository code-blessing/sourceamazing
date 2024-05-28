package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderProcessorApi
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.SchemaContextAccessor.toRevealedSchemaContext
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.builder.proxy.DataCollectorInvocationHandler
import kotlin.reflect.KClass

class BuilderProcessor(): BuilderProcessorApi {

    override fun <I : Any> withBuilder(schemaContext: SchemaContext, inputDefinitionClass: KClass<I>, builderUsage: (builder: I) -> Unit) {
        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(inputDefinitionClass)
        val schemaMemento = schemaContext.toRevealedSchemaContext()
        val builderImplementation: I = ProxyCreator.createProxy(inputDefinitionClass, DataCollectorInvocationHandler(schemaMemento.conceptDataCollector, emptyMap()))
        builderUsage(builderImplementation)
    }
}