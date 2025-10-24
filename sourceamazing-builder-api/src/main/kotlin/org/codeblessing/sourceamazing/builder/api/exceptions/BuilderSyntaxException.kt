package org.codeblessing.sourceamazing.builder.api.exceptions

import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage
import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException

open class BuilderSyntaxException(
    errorCodeWithMessage: ErrorCodeWithMessage,
    reasons: List<ErrorCodeWithMessage> = emptyList(),
) : SyntaxException(errorCodeWithMessage, reasons)
