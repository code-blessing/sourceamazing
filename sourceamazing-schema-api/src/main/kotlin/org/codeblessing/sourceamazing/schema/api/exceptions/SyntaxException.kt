package org.codeblessing.sourceamazing.schema.api.exceptions

import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCode

abstract class SyntaxException(val errorCode: ErrorCode, msg: String) : RuntimeException(msg)
