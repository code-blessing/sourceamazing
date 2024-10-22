package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.api.SchemaProcessorApi
import org.codeblessing.sourceamazing.schema.conceptgraph.ConceptResolver
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.schema.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.schema.filesystem.PhysicalFilesFileSystemAccess
import org.codeblessing.sourceamazing.schema.logger.JavaUtilLoggerFacade
import org.codeblessing.sourceamazing.schema.logger.LoggerFacade
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.codeblessing.sourceamazing.schema.schemacreator.query.proxy.SchemaInstanceInvocationHandler
import kotlin.reflect.KClass

class SchemaProcessor(
    private val fileSystemAccess: FileSystemAccess,
    private val loggerFacade: LoggerFacade,
): SchemaProcessorApi {
    constructor(fileSystemAccess: FileSystemAccess): this(fileSystemAccess, JavaUtilLoggerFacade(fileSystemAccess))
    @Suppress("unused")
    constructor(): this(PhysicalFilesFileSystemAccess())

    override fun <S : Any> withSchema(schemaDefinitionClass: KClass<S>, schemaUsage: (schemaContext: SchemaContext)-> Unit): S {
        val schemaAccess: SchemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaDefinitionClass)
        val conceptDataCollector = ConceptDataCollector(schemaAccess)
        val revealedSchemaContext = RevealedSchemaContext(schemaAccess, conceptDataCollector, fileSystemAccess, loggerFacade)

        schemaUsage(revealedSchemaContext)

        val conceptData: List<ConceptData> = conceptDataCollector.provideConceptData()
        val conceptGraph = ConceptResolver.validateAndResolveConcepts(schemaAccess, conceptData)
        val schemaInstance = ProxyCreator.createProxy(schemaDefinitionClass, SchemaInstanceInvocationHandler(conceptGraph))
        return schemaInstance

    }
}