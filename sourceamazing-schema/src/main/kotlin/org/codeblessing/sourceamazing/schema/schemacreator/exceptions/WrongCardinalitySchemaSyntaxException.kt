package org.codeblessing.sourceamazing.schema.schemacreator.exceptions

import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException

class WrongCardinalitySchemaSyntaxException(errorCode: SchemaErrorCode, vararg messageArguments: Any)
    : SyntaxException(errorCode, errorCode.format(*messageArguments))
