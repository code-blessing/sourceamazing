package org.codeblessing.sourceamazing.engine.extension

import org.codeblessing.sourceamazing.api.extensions.ExtensionName
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.ExtensionDataCollector
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionFromFilesExtension
import org.codeblessing.sourceamazing.api.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.api.logger.LoggerFacade
import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import java.nio.file.Path

class ExtensionHolder(
    private val fileSystemAccess: FileSystemAccess,
    private val loggerFacade: LoggerFacade,
    private val parameterAccess: ParameterAccess,
    private val schemaAccess: SchemaAccess,
    private val extensionDataCollector: ExtensionDataCollector,
): DataCollectionExtensionAccess {

    private val dataCollectionFromFilesExtensions: Map<ExtensionName, DataCollectionFromFilesExtension> =
        org.codeblessing.sourceamazing.engine.extension.ExtensionFinder.findAllDataCollectionFromFilesExtensions()
            .onEach { initializeDataCollectionExtension(it) }
            .associateBy { it.getExtensionName() }


    private fun initializeDataCollectionExtension(extension: DataCollectionFromFilesExtension) {
        extension.initializeDataCollectionExtension(
            loggerFacade = loggerFacade,
            parameterAccess = parameterAccess,
            extensionDataCollector = extensionDataCollector,
            fileSystemAccess = fileSystemAccess,
            schemaAccess = schemaAccess,
        )
    }

    override fun collectWithDataCollectionFromFilesExtension(extensionName: ExtensionName, inputFiles: Set<Path>) {
        dataCollectionFromFilesExtensions[extensionName]?.readFromFiles(inputFiles) ?: throwExtensionNotFound(extensionName)
    }

    private fun throwExtensionNotFound(extensionName: ExtensionName): Nothing {
        throw IllegalArgumentException("No extension for extension name ${extensionName.name}")
    }
}
