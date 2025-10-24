package org.codeblessing.sourceamazing.schema

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.api.SchemaProcessorApi
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.clazzgraph.ClazzInstance
import org.codeblessing.sourceamazing.schema.clazzgraph.ClazzInstanceGraph
import org.codeblessing.sourceamazing.schema.clazzgraph.ClazzResolver
import org.codeblessing.sourceamazing.schema.datacollection.TypeSafeClazzModelCollectorImpl
import org.codeblessing.sourceamazing.schema.proxy.ClazzInstanceFactory
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModel
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.adapter.TypeSafeSchemaContextAdapter

class SchemaProcessor : SchemaProcessorApi {

    override fun <S : Any> withSchema(rootClazz: KClass<S>, schemaUsage: (schemaContext: SchemaContext) -> Unit): S {
        val schemaAccess: TypeSafeSchemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(rootClazz)
        val clazzModelCollector = TypeSafeClazzModelCollectorImpl(schemaAccess)
        val schemaContextImpl = TypeSafeSchemaContextImpl(schemaAccess, clazzModelCollector)

        schemaUsage(TypeSafeSchemaContextAdapter(schemaContextImpl))

        val rootClazzModelData = clazzModelCollector.rootClazzModel()

        val clazzModelData: List<TypeSafeClazzModel> = clazzModelCollector.provideClazzModels()
        val clazzGraph = ClazzResolver.validateAndResolveClasses(schemaAccess, clazzModelData)
        val rootClazzNode = getRootClazzNode(rootClazzModelData.clazzModelId, clazzGraph)
        return ClazzInstanceFactory.createInstanceOrProxy(rootClazz, rootClazzNode)
    }

    private fun getRootClazzNode(
        rootClazzModelId: ClazzModelId,
        clazzInstanceGraph: ClazzInstanceGraph,
    ): ClazzInstance {
        return try {
            clazzInstanceGraph.instanceByClazzModelId(rootClazzModelId)
        } catch (_: NoSuchElementException) {
            throw DataValidationException(
                DataCollectionErrorCode.MISSING_ROOT_CLAZZ.withFormattedMessage(rootClazzModelId.name)
            )
        }
    }
}
