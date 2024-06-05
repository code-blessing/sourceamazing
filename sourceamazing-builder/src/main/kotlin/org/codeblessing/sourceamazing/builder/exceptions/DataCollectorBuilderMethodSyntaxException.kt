package org.codeblessing.sourceamazing.builder.exceptions

import java.lang.reflect.Method

class DataCollectorBuilderMethodSyntaxException(method: Method, msg: String)
    : DataCollectorBuilderException("$msg\n Method: $method")