package org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions

import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage
import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException

class SchemaSyntaxException(
    errorCodeWithMessage: ErrorCodeWithMessage,
    reasons: List<ErrorCodeWithMessage> = emptyList(),
) : SyntaxException(errorCodeWithMessage, reasons)
