package org.codeblessing.sourceamazing.schema.exceptions

import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import kotlin.reflect.KFunction

class WrongFunctionSyntaxException(function: KFunction<*>, errorCode: SchemaErrorCode, vararg messageArguments: Any)
    : SyntaxException(errorCode, "${errorCode.format(*messageArguments)}\n Function: $function")