package org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions

import org.codeblessing.sourceamazing.schema.ErrorCode

abstract class DataValidationException(val errorCode: ErrorCode, msg: String, cause: Exception? = null): RuntimeException(msg, cause)
