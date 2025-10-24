package org.codeblessing.sourceamazing.builder.exceptions

import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.api.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage

class BuilderMethodSyntaxException(
    methodLocation: MethodLocation,
    errorCodeWithMessage: ErrorCodeWithMessage,
    reasons: List<ErrorCodeWithMessage> = emptyList(),
) : BuilderSyntaxException(errorCodeWithMessage.appendLine(methodLocation.locationDescription()), reasons)
