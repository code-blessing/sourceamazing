package org.codeblessing.sourceamazing.api.process.conceptgraph.exceptions

abstract class ConceptGraphException(msg: String, cause: Exception?): RuntimeException(msg, cause) {
    constructor(msg: String): this(msg, null)
}
