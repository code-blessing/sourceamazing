package org.codeblessing.sourceamazing.schema.datacollection.validation

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.*
import org.codeblessing.sourceamazing.schema.datacollection.MultipleDataValidationException
import org.codeblessing.sourceamazing.schema.datacollection.validation.DataValidationExceptionCollector.Companion.collectAndMergeExceptions
import org.codeblessing.sourceamazing.schema.datacollection.validation.DataValidationExceptionCollector.Companion.collectAndThrowExceptions
import org.codeblessing.sourceamazing.utils.enumeration.EnumUtil
import org.codeblessing.sourceamazing.utils.type.enumValues

object FacetDataValidator {

    @Throws(MultipleDataValidationException::class, DataValidationException::class)
    fun validateFacets(schema: SchemaAccess, conceptDataMap: Map<ConceptIdentifier, ConceptData>) {
        forEachConcept(schema, conceptDataMap) { conceptSchema, conceptDataEntry, exceptionCollector ->
            checkNoObsoletFacets(conceptSchema, conceptDataEntry, exceptionCollector)
            checkIsValidFacetTypes(conceptSchema, conceptDataEntry, exceptionCollector)

            // if we have wrong facets and wrong types, we early return and
            // avoid validation errors that are based on wrong types, etc.
            if (exceptionCollector.isEmpty()) {
                checkCorrectFacetCardinality(conceptSchema, conceptDataEntry, exceptionCollector)
                checkNoWrongReferencedConcepts(conceptSchema, conceptDataEntry, conceptDataMap, exceptionCollector)
                checkNoMissingReferencedConcepts(conceptSchema, conceptDataEntry, conceptDataMap, exceptionCollector)
            }
        }
    }

    @Throws(MultipleDataValidationException::class, DataValidationException::class)
    fun validateFacetsWithoutReferencesAndCardinalities(schema: SchemaAccess, conceptDataEntry: ConceptData) {
        return collectAndThrowExceptions { exceptionCollector ->
            val conceptSchema =
                requireNotNull(schema.conceptByConceptName(conceptDataEntry.conceptName)) {
                    "Concept '${conceptDataEntry.conceptName}' does not exist"
                }
            checkNoObsoletFacets(conceptSchema, conceptDataEntry, exceptionCollector)
            checkIsValidFacetTypes(conceptSchema, conceptDataEntry, exceptionCollector)
        }
    }

    private fun checkNoObsoletFacets(
        conceptSchema: ConceptSchema,
        conceptDataEntry: ConceptData,
        exceptionCollector: DataValidationExceptionCollector,
    ) {
        // iterate through all entry facet values to find obsolet ones
        conceptDataEntry.getFacetNames().forEach { facetName ->
            exceptionCollector.catchAndCollectDataValidationExceptions {
                checkNoObsoleteFacet(conceptSchema, conceptDataEntry, facetName)
            }
        }
    }

    private fun checkNoObsoleteFacet(
        conceptSchema: ConceptSchema,
        conceptDataEntry: ConceptData,
        facetName: FacetName,
    ) {
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

    private fun checkIsValidFacetTypes(
        conceptSchema: ConceptSchema,
        conceptDataEntry: ConceptData,
        exceptionCollector: DataValidationExceptionCollector,
    ) {
        forEachFacetValue(conceptSchema, conceptDataEntry, exceptionCollector) { facetSchema, facetValue ->
            checkFacetDataType(conceptDataEntry, facetSchema, facetValue)
        }
    }

    private fun checkNoWrongReferencedConcepts(
        conceptSchema: ConceptSchema,
        conceptDataEntry: ConceptData,
        conceptDataMap: Map<ConceptIdentifier, ConceptData>,
        exceptionCollector: DataValidationExceptionCollector,
    ) {
        forEachFacetValue(conceptSchema, conceptDataEntry, exceptionCollector) { facetSchema, facetValue ->
            if (facetSchema is ReferenceFacetSchema) {
                val referencedConceptIdentifier = facetValue as ConceptIdentifier
                checkIsReferencedConceptPossible(
                    conceptDataMap,
                    conceptDataEntry,
                    facetSchema,
                    referencedConceptIdentifier,
                )
            }
        }
    }

    private fun checkIsReferencedConceptPossible(
        conceptDataMap: Map<ConceptIdentifier, ConceptData>,
        conceptDataEntry: ConceptData,
        facetSchema: ReferenceFacetSchema,
        referencedConceptIdentifier: ConceptIdentifier,
    ) {
        val possibleConcepts = facetSchema.referencingConcepts
        val referencedConcept = conceptDataMap[referencedConceptIdentifier]
        if (referencedConcept != null && referencedConcept.conceptName !in possibleConcepts) {
            throw WrongReferencedConceptFacetValueException(
                DataCollectionErrorCode.WRONG_REFERENCED_CONCEPT_FACET_VALUE,
                facetSchema.facetName,
                conceptDataEntry.conceptIdentifier.name,
                conceptDataEntry.conceptName,
                referencedConcept.conceptName,
                possibleConcepts,
                conceptDataEntry.describe(),
            )
        }
    }

    private fun checkNoMissingReferencedConcepts(
        conceptSchema: ConceptSchema,
        conceptDataEntry: ConceptData,
        conceptDataMap: Map<ConceptIdentifier, ConceptData>,
        exceptionCollector: DataValidationExceptionCollector,
    ) {
        forEachFacetValue(conceptSchema, conceptDataEntry, exceptionCollector) { facetSchema, facetValue ->
            if (facetSchema is ReferenceFacetSchema) {
                val referencedConceptIdentifier = facetValue as ConceptIdentifier
                checkIsReferencedConceptNotMissing(
                    conceptDataMap,
                    conceptDataEntry,
                    facetSchema,
                    referencedConceptIdentifier,
                )
            }
        }
    }

    private fun checkIsReferencedConceptNotMissing(
        conceptDataMap: Map<ConceptIdentifier, ConceptData>,
        conceptDataEntry: ConceptData,
        facetSchema: ReferenceFacetSchema,
        referencedConceptIdentifier: ConceptIdentifier,
    ) {
        val possibleConcepts = facetSchema.referencingConcepts
        val referencedConcept = conceptDataMap[referencedConceptIdentifier]
        if (referencedConcept == null) {
            throw MissingReferencedConceptFacetValueException(
                DataCollectionErrorCode.MISSING_REFERENCED_CONCEPT_FACET_VALUE,
                facetSchema.facetName,
                conceptDataEntry.conceptIdentifier.name,
                conceptDataEntry.conceptName,
                referencedConceptIdentifier,
                possibleConcepts,
                conceptDataEntry.describe(),
            )
        }
    }

    private fun checkFacetDataType(conceptDataEntry: ConceptData, facetSchema: FacetSchema, facetValue: Any) {
        val actualClass = facetValue::class
        val isValidType =
            when (facetSchema) {
                is TextFacetSchema -> actualClass == String::class
                is NumberFacetSchema -> actualClass == Int::class
                is BooleanFacetSchema -> actualClass == Boolean::class
                is ReferenceFacetSchema -> actualClass == ConceptIdentifier::class
                is EnumFacetSchema -> isValidEnumValue(facetValue, facetSchema)
            }

        if (!isValidType) {
            throw if (facetSchema is EnumFacetSchema && facetValue is String) {
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
                    facetSchema.facetType,
                    actualClass.longText(),
                    facetValue,
                    conceptDataEntry.describe(),
                )
            }
        }
    }

    private fun facetEnumType(facetSchema: EnumFacetSchema): KClass<*> {
        return requireNotNull(facetSchema.enumerationType) { "EnumerationType was empty for facet schema $facetSchema" }
    }

    private fun isValidEnumValue(enumFacetValue: Any, facetSchema: EnumFacetSchema): Boolean {
        val enumerationType = facetEnumType(facetSchema)
        return EnumUtil.fromAnyToEnum(enumFacetValue, enumerationType) != null
    }

    private fun checkCorrectFacetCardinality(
        conceptSchema: ConceptSchema,
        conceptDataEntry: ConceptData,
        exceptionCollector: DataValidationExceptionCollector,
    ) {
        forEachFacet(conceptSchema, exceptionCollector) { facetSchema ->
            checkMinimumOccurrences(facetSchema, conceptDataEntry)
        }
        forEachFacet(conceptSchema, exceptionCollector) { facetSchema ->
            checkMaximumOccurrences(facetSchema, conceptDataEntry)
        }
    }

    private fun checkMaximumOccurrences(facetSchema: FacetSchema, conceptDataEntry: ConceptData) {
        val maximumOccurrences = facetSchema.maximumOccurrences
        val numberOfFacetValues = conceptDataEntry.getFacet(facetSchema.facetName).size
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

    private fun checkMinimumOccurrences(facetSchema: FacetSchema, conceptDataEntry: ConceptData) {
        val minimumOccurrences = facetSchema.minimumOccurrences
        val numberOfFacetValues = conceptDataEntry.getFacet(facetSchema.facetName).size
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

    private fun forEachConcept(
        schema: SchemaAccess,
        conceptDataMap: Map<ConceptIdentifier, ConceptData>,
        block: (ConceptSchema, ConceptData, DataValidationExceptionCollector) -> Unit,
    ) {
        collectAndThrowExceptions { exceptionCollector ->
            conceptDataMap.values.forEach { conceptDataEntry ->
                val conceptSchema =
                    requireNotNull(schema.conceptByConceptName(conceptDataEntry.conceptName)) {
                        "Concept '${conceptDataEntry.conceptName}' does not exist."
                    }
                collectAndMergeExceptions(exceptionCollector) { conceptExceptionCollector ->
                    block(conceptSchema, conceptDataEntry, conceptExceptionCollector)
                }
            }
        }
    }

    private fun forEachFacet(
        conceptSchema: ConceptSchema,
        exceptionCollector: DataValidationExceptionCollector,
        block: (FacetSchema) -> Unit,
    ) {
        conceptSchema.facets.forEach { facetSchema ->
            exceptionCollector.catchAndCollectDataValidationExceptions { block(facetSchema) }
        }
    }

    private fun forEachFacetValue(
        conceptSchema: ConceptSchema,
        conceptDataEntry: ConceptData,
        exceptionCollector: DataValidationExceptionCollector,
        block: (FacetSchema, Any) -> Unit,
    ) {
        conceptSchema.facets.forEach { facetSchema ->
            if (conceptDataEntry.hasFacet(facetSchema.facetName)) {
                val facetValues = conceptDataEntry.getFacet(facetSchema.facetName)
                facetValues.forEach { facetValue ->
                    exceptionCollector.catchAndCollectDataValidationExceptions { block(facetSchema, facetValue) }
                }
            }
        }
    }

    private fun KClass<*>.longText(): String {
        return java.name
    }
}
