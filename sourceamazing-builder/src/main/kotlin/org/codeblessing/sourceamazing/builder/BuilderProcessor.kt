package org.codeblessing.sourceamazing.builder

import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.builder.alias.TypeSafeBuilderContextImpl
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.BuilderFactory
import org.codeblessing.sourceamazing.builder.api.BuilderProcessorApi
import org.codeblessing.sourceamazing.builder.factories.BuilderFactoriesHolder
import org.codeblessing.sourceamazing.builder.factories.BuilderFactoriesValidator
import org.codeblessing.sourceamazing.builder.proxy.BuilderProxyFactory
import org.codeblessing.sourceamazing.builder.validation.BuilderHierarchyValidator
import org.codeblessing.sourceamazing.schema.api.ClazzAndModelId
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.typesafeapi.TypeSafeClazzAndModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.TypeSafeSchemaContext
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModel
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.adapter.TypeSafeSchemaContextAdapter
import org.codeblessing.sourceamazing.schema.typesafeapi.toTypeSafeClazzAndModelId

private val DEFAULT_ROOT_ALIAS = Alias.of("root")

class BuilderProcessor() : BuilderProcessorApi {

    override fun <B : Any> withBuilder(
        schemaContext: SchemaContext,
        builderClass: KClass<B>,
        builderFactories: Set<BuilderFactory<*, *>>,
        rootAliases: Map<String, ClazzAndModelId>,
        builderUsage: (B) -> Unit,
    ) {
        val typeSafeSchemaContext = (schemaContext as TypeSafeSchemaContextAdapter).typeSafeSchemaContext
        val schemaAccess = typeSafeSchemaContext.schema
        val typeSafeRootAliases = createRootAliases(rootAliases, typeSafeSchemaContext)
        val builderContext = TypeSafeBuilderContextImpl(typeSafeRootAliases)

        val builderFactoriesHolder = BuilderFactoriesHolder(builderFactories)
        BuilderFactoriesValidator.validateBuilderFactories(builderFactoriesHolder)

        BuilderHierarchyValidator.validateTopLevelBuilderMethods(
            builderClass,
            builderFactoriesHolder,
            schemaAccess,
            builderContext.onlyWithClazzes(),
        )
        val builderProxyImplementation: B =
            BuilderProxyFactory.createNewBuilder(
                schemaContext = typeSafeSchemaContext,
                builderClass = builderClass,
                builderFactoriesHolder = builderFactoriesHolder,
                builderContext = builderContext,
            )
        builderUsage(builderProxyImplementation)
    }

    private fun createRootAliases(
        rootAliases: Map<String, ClazzAndModelId>,
        schemaContext: TypeSafeSchemaContext,
    ): Map<Alias, TypeSafeClazzAndModelId> {
        val typeSafeRootAliases =
            rootAliases
                .map { (key, value) -> Pair(key.toAlias(), value.toTypeSafeClazzAndModelId()) }
                .toMap()
                .toMutableMap()
        if (!typeSafeRootAliases.containsKey(DEFAULT_ROOT_ALIAS)) {
            typeSafeRootAliases[DEFAULT_ROOT_ALIAS] =
                schemaContext.dataCollector.rootClazzModel().toTypeSafeClazzAndModelId()
        }

        return typeSafeRootAliases
    }

    private fun TypeSafeClazzModel.toTypeSafeClazzAndModelId(): TypeSafeClazzAndModelId {
        return TypeSafeClazzAndModelId(clazz, clazzModelId)
    }
}
