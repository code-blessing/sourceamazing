package org.codeblessing.sourceamazing.schema.exceptions

import org.codeblessing.sourceamazing.schema.SchemaErrorCode

class WrongTypeSyntaxException(errorCode: SchemaErrorCode, vararg messageArguments: Any)
    : SyntaxException(errorCode, errorCode.format(*messageArguments))