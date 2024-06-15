package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.DataValidationException

class MultipleDataValidationException(private val exceptions: Set<DataValidationException>) :
    DataValidationException(
        DataCollectionErrorCode.MULTIPLE_DATA_VALIDATION_EXCEPTIONS,
        DataCollectionErrorCode.MULTIPLE_DATA_VALIDATION_EXCEPTIONS.format(exceptions.size),
        exceptions.firstOrNull()
    ) {

    override val message: String
        get() = concatMessages()

    private fun concatMessages(): String {
        return "${super.message}: ${this.exceptions}"
    }

}
