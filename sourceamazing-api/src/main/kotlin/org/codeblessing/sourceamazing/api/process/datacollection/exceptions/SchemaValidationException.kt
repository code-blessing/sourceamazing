package org.codeblessing.sourceamazing.api.process.datacollection.exceptions

abstract class SchemaValidationException(msg: String, cause: Exception?): RuntimeException(msg, cause) {
    constructor(msg: String): this(msg, null)
}
