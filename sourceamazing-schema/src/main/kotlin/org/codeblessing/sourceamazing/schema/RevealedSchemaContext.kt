package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.schema.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.schema.logger.LoggerFacade

class RevealedSchemaContext(
    val schema: SchemaAccess,
    val conceptDataCollector: ConceptDataCollector,
    val fileSystemAccess: FileSystemAccess,
    val loggerFacade: LoggerFacade,
): SchemaContext