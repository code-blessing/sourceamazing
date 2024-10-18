package org.codeblessing.sourceamazing.builder.exceptions

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import kotlin.reflect.KFunction

class BuilderMethodSyntaxException(method: KFunction<*>, errorCode: BuilderErrorCode, vararg messageArguments: Any)
    : SyntaxException(errorCode, "${errorCode.format(*messageArguments)}\n Method: $method")