package org.codeblessing.sourceamazing.schema.api.datacollection.exceptions

import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage
import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException

open class DataValidationException(
    errorCodeWithMessage: ErrorCodeWithMessage,
    reasons: List<ErrorCodeWithMessage> = emptyList(),
) : SyntaxException(errorCodeWithMessage, reasons)
