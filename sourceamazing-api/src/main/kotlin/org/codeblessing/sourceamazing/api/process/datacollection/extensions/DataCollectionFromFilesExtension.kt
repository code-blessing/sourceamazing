package org.codeblessing.sourceamazing.api.process.datacollection.extensions

import org.codeblessing.sourceamazing.api.extensions.Extension
import org.codeblessing.sourceamazing.api.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.api.logger.LoggerFacade
import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import java.nio.file.Path

interface DataCollectionFromFilesExtension: Extension {

    fun initializeDataCollectionExtension(
        loggerFacade: LoggerFacade,
        parameterAccess: ParameterAccess,
        fileSystemAccess: FileSystemAccess,
        schemaAccess: SchemaAccess,
        extensionDataCollector: ExtensionDataCollector,
    )

    fun readFromFiles(files: Set<Path>)
}
