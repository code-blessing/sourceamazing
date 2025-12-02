package org.codeblessing.sourceamazing.schema.datacollection.validation

import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.datacollection.MultipleDataValidationException

class DataValidationExceptionCollector private constructor() {
    private val listOfValidationFailures: MutableSet<DataValidationException> = mutableSetOf()

    /** @return true, if there were no caught exceptions. false otherwise */
    fun catchAndCollectDataValidationExceptions(validationExecution: () -> Unit): Boolean {
        try {
            validationExecution()
            return true
        } catch (ex: DataValidationException) {
            listOfValidationFailures.add(ex)
            return false
        }
    }

    private fun throwDataValidationException() {
        if (listOfValidationFailures.isEmpty()) {
            return
        } else if (listOfValidationFailures.size == 1) {
            throw listOfValidationFailures.single() // especially for tests
        } else {
            throw MultipleDataValidationException(listOfValidationFailures)
        }
    }

    fun isEmpty(): Boolean {
        return listOfValidationFailures.isEmpty()
    }

    fun merge(dataCollector: DataValidationExceptionCollector) {
        this.listOfValidationFailures.addAll(dataCollector.listOfValidationFailures)
    }

    companion object {
        fun <T> collectAndThrowExceptions(block: (DataValidationExceptionCollector) -> T): T {
            val exceptionCollector = DataValidationExceptionCollector()
            return block(exceptionCollector).also { exceptionCollector.throwDataValidationException() }
        }

        fun <T> collectAndMergeExceptions(
            parentExceptionCollector: DataValidationExceptionCollector,
            block: (DataValidationExceptionCollector) -> T,
        ): T {
            val exceptionCollector = DataValidationExceptionCollector()
            return block(exceptionCollector).also { parentExceptionCollector.merge(exceptionCollector) }
        }
    }
}
