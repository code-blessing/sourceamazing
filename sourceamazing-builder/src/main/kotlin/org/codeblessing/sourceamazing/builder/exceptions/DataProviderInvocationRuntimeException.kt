package org.codeblessing.sourceamazing.builder.exceptions

/**
 * It is important that this is a runtime exception!
 */
class DataProviderInvocationRuntimeException(
    msg: String,
    cause: Throwable,
): RuntimeException(msg, cause)