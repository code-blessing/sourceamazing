package org.codeblessing.sourceamazing.schema

interface ErrorCode {

    val messageFormat: String

    fun format(vararg messageArguments: Any): String {
        val numberOfPlaceholders = messageFormat.count { it == '%' }
        require(numberOfPlaceholders == messageArguments.size) {
            "$this: messageFormat must contain $numberOfPlaceholders placeholders but had ${messageArguments.size}: \"$messageFormat\" with ${messageArguments.toList()} "
        }

        return messageFormat.format(*messageArguments)
    }
}