package org.codeblessing.sourceamazing.schema

interface ErrorCode {

    class ErrorCodeFormattingException(message: String): Exception(message)

    val messageFormat: String

    fun format(vararg messageArguments: Any): String {
        val numberOfPlaceholders = messageFormat.count { it == '%' }
        if(numberOfPlaceholders != messageArguments.size) {
            throw ErrorCodeFormattingException("$this: messageFormat must contain $numberOfPlaceholders placeholders but had ${messageArguments.size}: \"$messageFormat\" with ${messageArguments.toList()} ")
        }

        return messageFormat.format(*messageArguments)
    }
}