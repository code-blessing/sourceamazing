package org.codeblessing.sourceamazing.builder.exceptions

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException

class BuilderMethodSyntaxException(methodLocation: MethodLocation, errorCode: BuilderErrorCode, vararg messageArguments: Any)
    : SyntaxException(errorCode, "${errorCode.format(*messageArguments)}\n${methodLocation.locationDescription()}")
