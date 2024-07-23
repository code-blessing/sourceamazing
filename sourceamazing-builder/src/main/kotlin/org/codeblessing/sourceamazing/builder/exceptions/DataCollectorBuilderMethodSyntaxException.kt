package org.codeblessing.sourceamazing.builder.exceptions

import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirror

class DataCollectorBuilderMethodSyntaxException(method: FunctionMirror, msg: String)
    : DataCollectorBuilderException("$msg\n Method: $method")