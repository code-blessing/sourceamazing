package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.ConceptData
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.api.SchemaProcessorApi
import org.codeblessing.sourceamazing.schema.conceptgraph.ConceptResolver
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollectorImpl
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.MissingRootConceptException
import org.codeblessing.sourceamazing.schema.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.schema.filesystem.PhysicalFilesFileSystemAccess
import org.codeblessing.sourceamazing.schema.logger.JavaUtilLoggerFacade
import org.codeblessing.sourceamazing.schema.logger.LoggerFacade
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.codeblessing.sourceamazing.schema.schemacreator.query.proxy.ConceptInstanceInvocationHandler
import kotlin.reflect.KClass

class SchemaProcessor(
    private val fileSystemAccess: FileSystemAccess,
    private val loggerFacade: LoggerFacade,
): SchemaProcessorApi {
    constructor(fileSystemAccess: FileSystemAccess): this(fileSystemAccess, JavaUtilLoggerFacade(fileSystemAccess))
    @Suppress("unused")
    constructor(): this(PhysicalFilesFileSystemAccess())

    override fun <S : Any> withSchema(schemaDefinitionClass: KClass<S>, schemaUsage: (schemaContext: SchemaContext)-> ConceptIdentifier): S {
        val schemaAccess: SchemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaDefinitionClass)
        val conceptDataCollector = ConceptDataCollectorImpl(schemaAccess)
        val revealedSchemaContext = RevealedSchemaContext(schemaAccess, conceptDataCollector, fileSystemAccess, loggerFacade)

        val rootConceptIdentifier = schemaUsage(revealedSchemaContext)

        val conceptData: List<ConceptData> = conceptDataCollector.provideConceptData()
        val conceptGraph = ConceptResolver.validateAndResolveConcepts(schemaAccess, conceptData)
        val rootConceptNode = try {
            conceptGraph.conceptByConceptIdentifier(rootConceptIdentifier)
        } catch(_: NoSuchElementException) {
            throw MissingRootConceptException(DataCollectionErrorCode.MISSING_ROOT_CONCEPT, rootConceptIdentifier.name)
        }
        val schemaInstance = ProxyCreator.createProxy(schemaDefinitionClass, ConceptInstanceInvocationHandler(rootConceptNode))
        return schemaInstance

    }
}
