package org.codeblessing.sourceamazing.builder.exceptions

import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class BuilderMethodParameterSyntaxException(method: KFunction<*>, methodParameter: KParameter, msg: String)
    : SyntaxException("$msg\n Method: $method, Parameter:${methodParameter.name}")