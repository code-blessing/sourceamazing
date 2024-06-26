package org.codeblessing.sourceamazing.schema.logger

import org.codeblessing.sourceamazing.schema.filesystem.FileSystemAccess
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

class JavaUtilLoggerFacade(fileSystemAccess: FileSystemAccess) : LoggerFacade {
    private val logger: Logger = Logger.getLogger("sourceamazing")

    init {
        // must set before the Logger
        // loads logging.properties from the classpath
        fileSystemAccess.classpathResourceAsInputStream("/sourceamazing-default-logging.properties").use {
            LogManager.getLogManager().readConfiguration(it)
        }

        try {
            fileSystemAccess.classpathResourceAsInputStream("/sourceamazing-logging.properties").use {
                LogManager.getLogManager().readConfiguration(it)
            }
        } catch (ex: Exception) {
            // ignore, the resource was not found
        }

    }

    override fun closeLoggerFacade() {
        setOf(
            *logger.handlers,
            *Logger.getGlobal().handlers,
            *Logger.getLogger("").handlers,
        ).forEach { loggerHandler ->
            loggerHandler.flush()
            loggerHandler.close()
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
