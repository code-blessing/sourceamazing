package org.codeblessing.sourceamazing.schema.datacollection.validation

import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.*
import org.codeblessing.sourceamazing.schema.datacollection.MultipleDataValidationException
import org.codeblessing.sourceamazing.schema.datacollection.validation.DataValidationExceptionCollector.Companion.collectAndMergeExceptions
import org.codeblessing.sourceamazing.schema.datacollection.validation.DataValidationExceptionCollector.Companion.collectAndThrowExceptions

object ConceptDataValidator {

    @Throws(MultipleDataValidationException::class, DataValidationException::class)
    fun validateEntries(
        schema: SchemaAccess,
        conceptDataEntries: List<ConceptData>,
    ): Map<ConceptIdentifier, ConceptData> {
        return collectAndThrowExceptions { exceptionCollector ->
            val validData: Map<ConceptIdentifier, ConceptData> =
                validateConceptNameAndIdentifier(schema, conceptDataEntries, exceptionCollector)

            exceptionCollector.catchAndCollectDataValidationExceptions {
                FacetDataValidator.validateFacets(schema, validData)
            }
            return@collectAndThrowExceptions validData
        }
    }

    @Throws(MultipleDataValidationException::class, DataValidationException::class)
    fun validateEntryWithoutReferenceAndCardinalityIntegrity(schema: SchemaAccess, conceptDataEntry: ConceptData) {
        return collectAndThrowExceptions { exceptionCollector ->
            exceptionCollector.catchAndCollectDataValidationExceptions {
                checkIsKnownConceptName(schema, conceptDataEntry)
            }
            exceptionCollector.catchAndCollectDataValidationExceptions {
                FacetDataValidator.validateFacetsWithoutReferencesAndCardinalities(schema, conceptDataEntry)
            }
        }
    }

    private fun validateConceptNameAndIdentifier(
        schema: SchemaAccess,
        conceptDataEntries: List<ConceptData>,
        exceptionCollector: DataValidationExceptionCollector,
    ): Map<ConceptIdentifier, ConceptData> {
        val validData: MutableMap<ConceptIdentifier, ConceptData> = mutableMapOf()

        val allConceptIdentifiers: MutableSet<ConceptIdentifier> = mutableSetOf()
        conceptDataEntries.forEach { conceptDataEntry ->
            collectAndMergeExceptions(exceptionCollector) { conceptExceptionCollector ->
                conceptExceptionCollector.catchAndCollectDataValidationExceptions {
                    checkIsKnownConceptName(schema, conceptDataEntry)
                }
                conceptExceptionCollector.catchAndCollectDataValidationExceptions {
                    checkIsNotDuplicateConceptIdentifier(allConceptIdentifiers, conceptDataEntry)
                }

                allConceptIdentifiers.add(conceptDataEntry.conceptIdentifier)
                if (conceptExceptionCollector.isEmpty()) {
                    validData[conceptDataEntry.conceptIdentifier] = conceptDataEntry
                }
            }
        }
        return validData
    }

    private fun checkIsKnownConceptName(schema: SchemaAccess, conceptDataEntry: ConceptData) {
        if (!schema.hasConceptName(conceptDataEntry.conceptName)) {
            throw UnknownConceptException(
                DataCollectionErrorCode.UNKNOWN_CONCEPT,
                conceptDataEntry.conceptIdentifier.name,
                conceptDataEntry.conceptName,
                conceptDataEntry.describe(),
            )
        }
    }

    fun checkIsNotDuplicateConceptIdentifier(
        allConceptIdentifiers: Set<ConceptIdentifier>,
        conceptDataEntry: ConceptData,
    ) {
        if (allConceptIdentifiers.contains(conceptDataEntry.conceptIdentifier)) {
            throw DuplicateConceptIdentifierException(
                DataCollectionErrorCode.DUPLICATE_CONCEPT_IDENTIFIER,
                conceptDataEntry.conceptIdentifier.name,
                conceptDataEntry.conceptName,
                conceptDataEntry.describe(),
            )
        }
    }
}
