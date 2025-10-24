package org.codeblessing.sourceamazing.schema.api.exceptions

data class ErrorCodeWithMessage(val errorCode: ErrorCode, val message: String) {
    fun appendLine(nextLine: String): ErrorCodeWithMessage {
        return ErrorCodeWithMessage(errorCode, "$message\n$nextLine")
    }
}
