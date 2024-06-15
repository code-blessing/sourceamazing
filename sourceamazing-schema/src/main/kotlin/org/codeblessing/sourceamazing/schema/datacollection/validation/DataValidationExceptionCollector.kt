package org.codeblessing.sourceamazing.schema.datacollection.validation

import org.codeblessing.sourceamazing.schema.datacollection.MultipleDataValidationException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.DataValidationException

class DataValidationExceptionCollector {
    private val listOfValidationFailures: MutableSet<DataValidationException> = mutableSetOf()

    /**
     * @return true, if there were no caught exceptions. false otherwise
     */
    fun catchAndCollectDataValidationExceptions(validationExecution: () -> Unit): Boolean {
        try {
            validationExecution()
            return true
        } catch (ex: DataValidationException) {
            listOfValidationFailures.add(ex)
            return false
        }
    }

    fun throwDataValidationException() {
        if(listOfValidationFailures.isEmpty()) {
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
}
