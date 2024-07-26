package org.codeblessing.sourceamazing.builder.exceptions

import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface

class DataCollectorBuilderMethodSyntaxException(method: FunctionMirrorInterface, msg: String)
    : DataCollectorBuilderException("$msg\n Method: $method")