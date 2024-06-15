package org.codeblessing.sourceamazing.schema.schemacreator.exceptions

import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import kotlin.reflect.KFunction

class WrongConceptQuerySchemaSyntaxException(function: KFunction<*>, errorCode: SchemaErrorCode, vararg messageArguments: Any)
    : SyntaxException(errorCode, "${errorCode.format(*messageArguments)}\n Function: $function")
