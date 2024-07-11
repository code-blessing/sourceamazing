package org.codeblessing.sourceamazing.builder.exceptions

import org.codeblessing.sourceamazing.schema.typemirror.MethodMirror

class DataCollectorBuilderMethodSyntaxException(method: MethodMirror, msg: String)
    : DataCollectorBuilderException("$msg\n Method: $method")