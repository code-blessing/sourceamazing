package org.codeblessing.sourceamazing.schema.api.exceptions

abstract class SyntaxException(val errorCode: ErrorCode, msg: String) : RuntimeException(msg)
