package org.codeblessing.sourceamazing.engine.process

import org.codeblessing.sourceamazing.api.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.api.logger.LoggerFacade
import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.DomainUnit
import org.codeblessing.sourceamazing.engine.filesystem.PhysicalFilesFileSystemAccess
import org.codeblessing.sourceamazing.engine.logger.JavaUtilLoggerFacade
import org.codeblessing.sourceamazing.engine.parameters.*

class ProcessSession(
    val domainUnits: List<DomainUnit<*, *>> = emptyList(),
    val fileSystemAccess: FileSystemAccess = PhysicalFilesFileSystemAccess(),
    val loggerFacade: LoggerFacade = JavaUtilLoggerFacade(fileSystemAccess),
    private val parameterSources: List<ParameterSource> = listOf(
        DefaultPropertyFileParameterSource(fileSystemAccess),
        EnvironmentVariablesParameterSource,
        SystemPropertyParameterSource,
    ),
    val parameterAccess: ParameterAccess = MultipleSourcesParameterAccess(parameterSources),
)
