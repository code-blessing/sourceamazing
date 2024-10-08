package org.codeblessing.sourceamazing.schema.exceptions

import kotlin.reflect.KFunction

class WrongFunctionSyntaxException(function: KFunction<*>, msg: String) : SyntaxException("$msg (Function: $function)")