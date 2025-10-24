package org.codeblessing.sourceamazing.schema.api.exceptions

abstract class SyntaxException(
    errorCodeWithMessage: ErrorCodeWithMessage,
    val reasons: List<ErrorCodeWithMessage> = emptyList(),
) : RuntimeException(errorCodeWithMessage.message) {
    val errorCode: ErrorCode = errorCodeWithMessage.errorCode
}
