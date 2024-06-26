package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.SchemaValidationException

class MultipleSchemaValidationException(private val exceptions: Set<SchemaValidationException>) :
    SchemaValidationException("There where ${exceptions.size} schema validation exceptions.", exceptions.firstOrNull()) {

    override val message: String
        get() = concatMessages()

    private fun concatMessages(): String {
        return "${super.message}: ${this.exceptions}"
    }

}
