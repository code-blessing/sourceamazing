package org.codeblessing.sourceamazing.schema

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.MissingRootConceptException
import org.codeblessing.sourceamazing.schema.conceptgraph.ConceptResolver
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollectorImpl
import org.codeblessing.sourceamazing.schema.proxy.ConceptInstanceInvocationHandler
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.codeblessing.sourceamazing.utils.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.utils.filesystem.PhysicalFilesFileSystemAccess
import org.codeblessing.sourceamazing.utils.logger.JavaUtilLoggerFacade
import org.codeblessing.sourceamazing.utils.logger.LoggerFacade
import org.codeblessing.sourceamazing.utils.proxy.ProxyCreator

class SchemaProcessor(private val fileSystemAccess: FileSystemAccess, private val loggerFacade: LoggerFacade) :
    SchemaProcessorApi {
    constructor(fileSystemAccess: FileSystemAccess) : this(fileSystemAccess, JavaUtilLoggerFacade(fileSystemAccess))

    @Suppress("unused") constructor() : this(PhysicalFilesFileSystemAccess())

    override fun <S : Any> withSchema(
        schemaDefinitionClass: KClass<S>,
        schemaUsage: (schemaContext: SchemaContext) -> ConceptIdentifier,
    ): S {
        val schemaAccess: SchemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaDefinitionClass)
        val conceptDataCollector = ConceptDataCollectorImpl(schemaAccess)
        val revealedSchemaContext =
            RevealedSchemaContext(schemaAccess, conceptDataCollector, fileSystemAccess, loggerFacade)

        val rootConceptIdentifier = schemaUsage(revealedSchemaContext)

        val conceptData: List<ConceptData> = conceptDataCollector.provideConceptData()
        val conceptGraph = ConceptResolver.validateAndResolveConcepts(schemaAccess, conceptData)
        val rootConceptNode =
            try {
                conceptGraph.conceptByConceptIdentifier(rootConceptIdentifier)
            } catch (_: NoSuchElementException) {
                throw MissingRootConceptException(
                    DataCollectionErrorCode.MISSING_ROOT_CONCEPT,
                    rootConceptIdentifier.name,
                )
            }
        val schemaInstance =
            ProxyCreator.createProxy(schemaDefinitionClass, ConceptInstanceInvocationHandler(rootConceptNode))
        return schemaInstance
    }
}
