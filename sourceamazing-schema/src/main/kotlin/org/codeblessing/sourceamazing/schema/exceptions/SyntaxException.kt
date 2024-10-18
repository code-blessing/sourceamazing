package org.codeblessing.sourceamazing.schema.exceptions

import org.codeblessing.sourceamazing.schema.ErrorCode

abstract class SyntaxException(val errorCode: ErrorCode, msg: String) : RuntimeException(msg)
