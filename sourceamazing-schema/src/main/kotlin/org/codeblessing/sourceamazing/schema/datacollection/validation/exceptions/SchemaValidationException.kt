package org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions

abstract class SchemaValidationException(msg: String, cause: Exception?): RuntimeException(msg, cause) {
    constructor(msg: String): this(msg, null)
}
