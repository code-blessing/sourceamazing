package org.codeblessing.sourceamazing.engine.process.datacollection.validation.exceptions

abstract class SchemaValidationException(msg: String, cause: Exception?): RuntimeException(msg, cause) {
    constructor(msg: String): this(msg, null)
}
