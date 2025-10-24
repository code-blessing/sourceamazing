package org.codeblessing.sourceamazing.schema.datacollection.validation

import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage

class DataValidationFailureCollector private constructor() {
    private val listOfValidationFailures: MutableList<ErrorCodeWithMessage> = mutableListOf()

    fun withSubCollector(block: (DataValidationFailureCollector) -> Unit) {
        val subCollector = DataValidationFailureCollector()
        block(subCollector)
        this.merge(subCollector)
    }

    private fun throwDataValidationException() {
        if (listOfValidationFailures.isEmpty()) {
            return
        } else {
            throw DataValidationException(
                DataCollectionErrorCode.VALIDATION_FAILURES.withFormattedMessage(
                    listOfValidationFailures.size,
                    listOfValidationFailures.joinToString("\n") { it.message },
                ),
                listOfValidationFailures,
            )
        }
    }

    fun isEmpty(): Boolean {
        return listOfValidationFailures.isEmpty()
    }

    fun merge(dataCollector: DataValidationFailureCollector) {
        this.listOfValidationFailures.addAll(dataCollector.listOfValidationFailures)
    }

    fun add(errorCode: DataCollectionErrorCode, message: String) {
        listOfValidationFailures.add(ErrorCodeWithMessage(errorCode, message))
    }

    companion object {
        fun <T> collectAndThrowExceptionOnValidationFailures(block: (DataValidationFailureCollector) -> T): T {
            val exceptionCollector = DataValidationFailureCollector()
            return block(exceptionCollector).also { exceptionCollector.throwDataValidationException() }
        }
    }
}
