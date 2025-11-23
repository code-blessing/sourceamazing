package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollectorImpl
import org.codeblessing.sourceamazing.utils.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.utils.logger.LoggerFacade

class RevealedSchemaContext(
    override val schema: SchemaAccess,
    override val dataCollector: ConceptDataCollectorImpl,
    val fileSystemAccess: FileSystemAccess,
    val loggerFacade: LoggerFacade,
) : SchemaContext
