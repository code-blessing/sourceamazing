package org.codeblessing.sourceamazing.api.logger

interface LoggerFacade {
    fun logDebug(msg: String)
    fun logDebug(msgProvider: () -> String)
    fun logUserInfo(msg: String)
    fun logUserInfo(msgProvider: () -> String)
    fun logWarnings(msg: String)
    fun logWarnings(msgProvider: () -> String)
}
