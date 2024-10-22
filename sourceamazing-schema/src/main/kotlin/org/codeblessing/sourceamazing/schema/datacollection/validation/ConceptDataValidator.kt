package org.codeblessing.sourceamazing.schema.datacollection.validation

import org.codeblessing.sourceamazing.schema.ConceptData
import org.codeblessing.sourceamazing.schema.ConceptSchema
import org.codeblessing.sourceamazing.schema.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.FacetSchema
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.datacollection.MultipleDataValidationException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.DuplicateConceptIdentifierException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.MissingReferencedConceptFacetValueException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.UnknownConceptException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.UnknownFacetNameException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.WrongCardinalityForFacetValueException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.WrongReferencedConceptFacetValueException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.WrongTypeForFacetValueException
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.type.enumValues
import kotlin.reflect.KClass


object ConceptDataValidator {

    @Throws(MultipleDataValidationException::class, DataValidationException::class)
    fun validateEntries(
        schema: SchemaAccess,
        conceptDataEntries: List<ConceptData>
    ): Map<ConceptIdentifier, ConceptData> {
        val exceptionCollector = DataValidationExceptionCollector()

        val validConceptIdentifierWithConceptName: Map<ConceptIdentifier, ConceptData> =
            validateConceptNameAndIdentifier(schema, conceptDataEntries, exceptionCollector)

        validateFacets(schema, validConceptIdentifierWithConceptName, exceptionCollector)

        exceptionCollector.throwDataValidationException()
        return validConceptIdentifierWithConceptName
    }

    fun validateEntryWithoutReferenceAndCardinalityIntegrity(
        schema: SchemaAccess,
        conceptDataEntry: ConceptData
    ) {
        val exceptionCollector = DataValidationExceptionCollector()
        exceptionCollector.catchAndCollectDataValidationExceptions {
            validateConceptName(schema, conceptDataEntry)
        }
        validateFacetsWithoutReferencesAndCardinalities(schema, conceptDataEntry, exceptionCollector)
        exceptionCollector.throwDataValidationException()
    }

    private fun validateConceptNameAndIdentifier(
        schema: SchemaAccess,
        conceptDataEntries: List<ConceptData>,
        exceptionCollector: DataValidationExceptionCollector
    ): Map<ConceptIdentifier, ConceptData> {
        val validConceptIdentifierWithConceptName: MutableMap<ConceptIdentifier, ConceptData> = mutableMapOf()

        val allConceptIdentifiers: MutableSet<ConceptIdentifier> = mutableSetOf()
        conceptDataEntries.forEach { conceptDataEntry ->
            val exceptionCollectorForConcept = DataValidationExceptionCollector()

            exceptionCollectorForConcept.catchAndCollectDataValidationExceptions {
                validateConceptName(schema, conceptDataEntry)
            }
            exceptionCollectorForConcept.catchAndCollectDataValidationExceptions {
                validateDuplicateConceptIdentifiers(allConceptIdentifiers, conceptDataEntry)
            }

            allConceptIdentifiers.add(conceptDataEntry.conceptIdentifier)
            if (exceptionCollectorForConcept.isEmpty()) {
                validConceptIdentifierWithConceptName[conceptDataEntry.conceptIdentifier] = conceptDataEntry
            }
            exceptionCollector.merge(exceptionCollectorForConcept)
        }
        return validConceptIdentifierWithConceptName
    }

    private fun validateFacetsWithoutReferencesAndCardinalities(
        schema: SchemaAccess,
        conceptDataEntry: ConceptData,
        exceptionCollector: DataValidationExceptionCollector
    ) {
        val conceptSchema = schema.conceptByConceptName(conceptDataEntry.conceptName)
        validateForObsoletFacets(conceptSchema, conceptDataEntry, exceptionCollector)
        validateForFacetType(conceptSchema, conceptDataEntry, exceptionCollector)
    }

    private fun validateFacets(
        schema: SchemaAccess,
        conceptDataMap: Map<ConceptIdentifier, ConceptData>,
        exceptionCollector: DataValidationExceptionCollector
    ) {
        conceptDataMap.values.forEach { conceptDataEntry ->
            val conceptSchema = schema.conceptByConceptName(conceptDataEntry.conceptName)
            val exceptionCollectorForConcept = DataValidationExceptionCollector()

            validateForObsoletFacets(conceptSchema, conceptDataEntry, exceptionCollectorForConcept)
            validateForFacetType(conceptSchema, conceptDataEntry, exceptionCollectorForConcept)

            // if we have wrong facets and wrong types, we early return and
            // avoid validation errors that are based on wrong types, etc.
            if (exceptionCollectorForConcept.isEmpty()) {
                validateForFacetCardinality(conceptSchema, conceptDataEntry, exceptionCollectorForConcept)
                validateForReferenceFacetWithWrongConcepts(conceptSchema, conceptDataEntry, conceptDataMap, exceptionCollectorForConcept)
                validateForReferenceFacetWithMissingConcepts(conceptSchema, conceptDataEntry, conceptDataMap, exceptionCollectorForConcept)
            }
            exceptionCollector.merge(exceptionCollectorForConcept)
        }
    }

    private fun validateForReferenceFacetWithMissingConcepts(
        conceptSchema: ConceptSchema,
        conceptDataEntry: ConceptData,
        conceptDataMap: Map<ConceptIdentifier, ConceptData>,
        exceptionCollector: DataValidationExceptionCollector
    ) {
        conceptSchema.facets
            .filter { it.facetType == FacetType.REFERENCE }
            .forEach { referenceFacetSchema ->
                val facetValues = conceptDataEntry.getFacet(referenceFacetSchema.facetName)
                val possibleConcepts = referenceFacetSchema.referencingConcepts
                facetValues.forEach { facetValue ->
                    exceptionCollector.catchAndCollectDataValidationExceptions {
                        val referenceConceptIdentifier = facetValue as ConceptIdentifier
                        val referencedConcept = conceptDataMap[referenceConceptIdentifier]
                        if (referencedConcept == null) {
                            throw MissingReferencedConceptFacetValueException(
                                DataCollectionErrorCode.MISSING_REFERENCED_CONCEPT_FACET_VALUE,
                                referenceFacetSchema.facetName,
                                conceptDataEntry.conceptIdentifier.name,
                                conceptDataEntry.conceptName,
                                referenceConceptIdentifier,
                                possibleConcepts,
                                conceptDataEntry.describe(),
                            )
                        }
                    }
                }
            }
    }

    private fun validateForReferenceFacetWithWrongConcepts(
        conceptSchema: ConceptSchema,
        conceptDataEntry: ConceptData,
        conceptDataMap: Map<ConceptIdentifier, ConceptData>,
        exceptionCollector: DataValidationExceptionCollector
    ) {
        conceptSchema.facets
            .filter { it.facetType == FacetType.REFERENCE }
            .forEach { referenceFacetSchema ->
                val facetValues = conceptDataEntry.getFacet(referenceFacetSchema.facetName)
                val possibleConcepts = referenceFacetSchema.referencingConcepts
                facetValues.forEach { facetValue ->
                    exceptionCollector.catchAndCollectDataValidationExceptions {
                        val referenceConceptIdentifier = facetValue as ConceptIdentifier
                        val referencedConcept = conceptDataMap[referenceConceptIdentifier]
                        if (referencedConcept != null && referencedConcept.conceptName !in possibleConcepts) {
                            throw WrongReferencedConceptFacetValueException(
                                DataCollectionErrorCode.WRONG_REFERENCED_CONCEPT_FACET_VALUE,
                                referenceFacetSchema.facetName,
                                conceptDataEntry.conceptIdentifier.name,
                                conceptDataEntry.conceptName,
                                referencedConcept.conceptName,
                                possibleConcepts,
                                conceptDataEntry.describe(),
                            )
                        }
                    }
                }
            }
    }

    private fun validateConceptName(
        schema: SchemaAccess,
        conceptDataEntry: ConceptData
    ) {
        if (!schema.hasConceptName(conceptDataEntry.conceptName)) {
            throw UnknownConceptException(
                DataCollectionErrorCode.UNKNOWN_CONCEPT,
                conceptDataEntry.conceptIdentifier.name,
                conceptDataEntry.conceptName,
                conceptDataEntry.describe(),
            )
        }
    }

    fun validateDuplicateConceptIdentifiers(
        allConceptIdentifiers: Set<ConceptIdentifier>,
        conceptDataEntry: ConceptData
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

    private fun validateForObsoletFacets(
        conceptSchema: ConceptSchema,
        conceptDataEntry: ConceptData,
        exceptionCollector: DataValidationExceptionCollector
    ) {
        // iterate through all entry facet values to find obsolet ones
        conceptDataEntry.getFacetNames().forEach { facetName ->
            exceptionCollector.catchAndCollectDataValidationExceptions {
                if (!conceptSchema.hasFacet(facetName)) {
                    throw UnknownFacetNameException(
                        DataCollectionErrorCode.UNKNOWN_FACET,
                        facetName,
                        conceptDataEntry.conceptIdentifier.name,
                        conceptDataEntry.conceptName,
                        conceptSchema.facetNames,
                        conceptDataEntry.describe(),
                    )
                }
            }
        }
    }

    private fun validateForFacetType(
        conceptSchema: ConceptSchema,
        conceptDataEntry: ConceptData,
        exceptionCollector: DataValidationExceptionCollector
    ) {
        conceptSchema.facets.forEach { facetSchema ->
            if (conceptDataEntry.hasFacet(facetSchema.facetName)) {
                val facetValues = conceptDataEntry.getFacet(facetSchema.facetName)
                val expectedFacetType = facetSchema.facetType
                facetValues.forEach { facetValue ->
                    exceptionCollector.catchAndCollectDataValidationExceptions {
                        validateDataType(conceptDataEntry, facetSchema, expectedFacetType, facetValue)
                    }
                }
            }
        }
    }

    private fun validateDataType(
        conceptDataEntry: ConceptData,
        facetSchema: FacetSchema,
        expectedFacetType: FacetType,
        facetValue: Any,
    ) {
        val actualClass = facetValue::class
        val isValidType = when (expectedFacetType) {
            FacetType.TEXT -> actualClass == String::class
            FacetType.NUMBER -> actualClass == Int::class
            FacetType.BOOLEAN -> actualClass == Boolean::class
            FacetType.REFERENCE -> actualClass == ConceptIdentifier::class
            FacetType.TEXT_ENUMERATION -> isValidEnumValue(facetValue, facetSchema)
        }

        if (!isValidType) {
            throw if (expectedFacetType == FacetType.TEXT_ENUMERATION && facetValue is String) {
                WrongTypeForFacetValueException(
                    DataCollectionErrorCode.WRONG_FACET_ENUM_TYPE,
                    facetSchema.facetName,
                    conceptDataEntry.conceptIdentifier.name,
                    conceptDataEntry.conceptName,
                    facetEnumType(facetSchema).enumValues,
                    facetValue,
                    actualClass.longText(),
                    conceptDataEntry.describe(),
                )
            } else {
                WrongTypeForFacetValueException(
                    DataCollectionErrorCode.WRONG_FACET_TYPE,
                    facetSchema.facetName,
                    conceptDataEntry.conceptIdentifier.name,
                    conceptDataEntry.conceptName,
                    expectedFacetType,
                    actualClass.longText(),
                    facetValue,
                    conceptDataEntry.describe(),
                )
            }
        }
    }

    private fun facetEnumType(facetSchema: FacetSchema): KClass<*> {
        return requireNotNull(facetSchema.enumerationType) {
            "EnumerationType was empty for facet schema $facetSchema"
        }
    }

    private fun isValidEnumValue(enumFacetValue: Any, facetSchema: FacetSchema): Boolean {
        val enumerationType = facetEnumType(facetSchema)
        return when (enumFacetValue) {
            is Enum<*> -> enumerationType.enumValues.contains(enumFacetValue)
            is String -> enumerationType.enumValues.map { it.name }.contains(enumFacetValue)
            else -> false
        }
    }

    private fun validateForFacetCardinality(
        schemaConcept: ConceptSchema,
        conceptDataEntry: ConceptData,
        exceptionCollector: DataValidationExceptionCollector
    ) {
        schemaConcept.facets.forEach { facetSchema ->
            val minimumOccurrences = facetSchema.minimumOccurrences
            val maximumOccurrences = facetSchema.maximumOccurrences
            val numberOfFacetValues = conceptDataEntry.getFacet(facetSchema.facetName).size
            exceptionCollector.catchAndCollectDataValidationExceptions {
                if (numberOfFacetValues < minimumOccurrences) {
                    throw WrongCardinalityForFacetValueException(
                        DataCollectionErrorCode.MINIMUM_CARDINALITY_ERROR,
                        facetSchema.facetName,
                        conceptDataEntry.conceptIdentifier.name,
                        conceptDataEntry.conceptName,
                        minimumOccurrences,
                        numberOfFacetValues,
                        conceptDataEntry.describe(),
                    )
                }
            }
            exceptionCollector.catchAndCollectDataValidationExceptions {
                if (numberOfFacetValues > maximumOccurrences) {
                    throw WrongCardinalityForFacetValueException(
                        DataCollectionErrorCode.MAXIMUM_CARDINALITY_ERROR,
                        facetSchema.facetName,
                        conceptDataEntry.conceptIdentifier.name,
                        conceptDataEntry.conceptName,
                        maximumOccurrences,
                        numberOfFacetValues,
                        conceptDataEntry.describe(),
                    )
                }
            }
        }
    }
}

