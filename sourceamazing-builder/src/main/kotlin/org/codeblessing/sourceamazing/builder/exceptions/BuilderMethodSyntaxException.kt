package org.codeblessing.sourceamazing.builder.exceptions

import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import kotlin.reflect.KFunction

class BuilderMethodSyntaxException(method: KFunction<*>, msg: String)
    : SyntaxException("$msg\n Method: $method")