package org.codeblessing.sourceamazing.engine.logger

import org.codeblessing.sourceamazing.api.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.api.logger.LoggerFacade
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

class JavaUtilLoggerFacade(fileSystemAccess: FileSystemAccess) : LoggerFacade {
    private val logger: Logger = Logger.getLogger("sourceamazing")

    init {
        // must set before the Logger
        // loads logging.properties from the classpath
        fileSystemAccess.classpathResourceAsInputStream("/sourceamazing-logging.properties").use {
            LogManager.getLogManager().readConfiguration(it)
        }
    }
    override fun logDebug(msg: String) {
        logger.log(Level.FINE, msg)
    }

    override fun logDebug(msgProvider: () -> String) {
        logger.log(Level.FINE, msgProvider)
    }

    override fun logUserInfo(msg: String) {
        logger.log(Level.INFO, msg)
    }

    override fun logUserInfo(msgProvider: () -> String) {
        logger.log(Level.INFO, msgProvider)
    }

    override fun logWarnings(msg: String) {
        logger.log(Level.WARNING, msg)
    }

    override fun logWarnings(msgProvider: () -> String) {
        logger.log(Level.WARNING, msgProvider)
    }

}
