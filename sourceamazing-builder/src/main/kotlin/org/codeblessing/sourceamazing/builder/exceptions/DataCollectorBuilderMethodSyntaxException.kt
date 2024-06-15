package org.codeblessing.sourceamazing.builder.exceptions

import kotlin.reflect.KFunction

class DataCollectorBuilderMethodSyntaxException(method: KFunction<*>, msg: String)
    : DataCollectorBuilderException("$msg\n Method: $method")