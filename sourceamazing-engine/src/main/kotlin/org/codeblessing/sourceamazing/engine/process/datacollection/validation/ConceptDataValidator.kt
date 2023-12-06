package org.codeblessing.sourceamazing.engine.process.datacollection.validation

import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptSchema
import org.codeblessing.sourceamazing.api.process.schema.FacetSchema
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.api.process.schema.annotations.FacetType
import org.codeblessing.sourceamazing.engine.process.datacollection.MultipleSchemaValidationException
import org.codeblessing.sourceamazing.engine.process.datacollection.validation.exceptions.*
import org.codeblessing.sourceamazing.engine.process.util.EnumUtil

object ConceptDataValidator {

    @Throws(MultipleSchemaValidationException::class, SchemaValidationException::class)
    fun validateEntries(
        schema: SchemaAccess,
        conceptDataEntries: List<ConceptData>
    ): Map<ConceptIdentifier, ConceptData> {
        val listOfValidationFailures: MutableSet<SchemaValidationException> = mutableSetOf()

        val validConceptIdentifierWithConceptName: Map<ConceptIdentifier, ConceptData>
            = validateConceptNameAndIdentifier(schema, conceptDataEntries, listOfValidationFailures)

        validateFacets(schema, validConceptIdentifierWithConceptName, listOfValidationFailures)

        if(listOfValidationFailures.isEmpty()) {
            return validConceptIdentifierWithConceptName
        } else if (listOfValidationFailures.size == 1) {
            throw listOfValidationFailures.single() // especially for tests
        } else {
            throw MultipleSchemaValidationException(listOfValidationFailures)
        }
    }

    private fun validateConceptNameAndIdentifier(
        schema: SchemaAccess,
        conceptDataEntries: List<ConceptData>,
        listOfValidationFailures: MutableSet<SchemaValidationException>
    ): Map<ConceptIdentifier, ConceptData> {
        val validConceptIdentifierWithConceptName: MutableMap<ConceptIdentifier, ConceptData> = mutableMapOf()

        val allConceptIdentifiers: MutableSet<ConceptIdentifier> = mutableSetOf()
        conceptDataEntries.forEach { conceptDataEntry ->
            var isValidConcept = true

            validateConceptName(schema, conceptDataEntry)
                ?.let(listOfValidationFailures::add)
                ?.let { isValidConcept = false }

            validateDuplicateConceptIdentifiers(allConceptIdentifiers, conceptDataEntry)
                ?.let(listOfValidationFailures::add)
                ?.let { isValidConcept = false }

            allConceptIdentifiers.add(conceptDataEntry.conceptIdentifier)
            if(isValidConcept) {
                validConceptIdentifierWithConceptName[conceptDataEntry.conceptIdentifier] = conceptDataEntry
            }
        }
        return validConceptIdentifierWithConceptName
    }

    private fun validateFacets(
        schema: SchemaAccess,
        conceptDataMap: Map<ConceptIdentifier, ConceptData>,
        listOfValidationFailures: MutableSet<SchemaValidationException>
    ) {
        conceptDataMap.values.forEach { conceptDataEntry ->
            val conceptSchema = schema.conceptByConceptName(conceptDataEntry.conceptName)
            var skipDeeperValidation = false
            validateForObsoletFacets(conceptSchema, conceptDataEntry)
                ?.let(listOfValidationFailures::add)
                ?.let { skipDeeperValidation = true }

            validateForFacetType(conceptSchema, conceptDataEntry)
                ?.let(listOfValidationFailures::addAll)
                ?.let { skipDeeperValidation = true }

            // if we have wrong facets and wrong types, we early return and
            // avoid validation errors that are based on wrong types, etc.
            if(!skipDeeperValidation) {
                validateForFacetCardinality(conceptSchema, conceptDataEntry)
                    ?.let(listOfValidationFailures::addAll)

                validateForReferenceFacet(conceptSchema, conceptDataEntry, conceptDataMap)
                    ?.let(listOfValidationFailures::addAll)
            }
        }
    }

    private fun validateForReferenceFacet(
        conceptSchema: ConceptSchema,
        conceptData: ConceptData,
        conceptDataMap: Map<ConceptIdentifier, ConceptData>
    ): List<SchemaValidationException>? {
        val exceptionList = mutableListOf<SchemaValidationException>()
        conceptSchema.facets
            .filter { it.facetType == FacetType.REFERENCE }
            .forEach { referenceFacetSchema ->
                val facetValues = conceptData.getFacet(referenceFacetSchema.facetName)
                val possibleConcepts = referenceFacetSchema.referencingConcepts
                facetValues.forEach { facetValue ->
                    val referenceConceptIdentifier = facetValue as ConceptIdentifier
                    val referencedConcept = conceptDataMap[referenceConceptIdentifier]
                    if(referencedConcept == null) {
                        exceptionList.add(
                            MissingReferencedConceptFacetValueException(
                            concept = conceptData.conceptName,
                            conceptIdentifier = conceptData.conceptIdentifier,
                            facetName = referenceFacetSchema.facetName,
                            reason = "No concept with concept id $referenceConceptIdentifier. " +
                                    "Must be one of these concepts ${possibleConcepts}."
                        )
                        )
                    } else if(!possibleConcepts.contains(referencedConcept.conceptName)) {
                        exceptionList.add(
                            WrongReferencedConceptFacetValueException(
                            concept = conceptData.conceptName,
                            conceptIdentifier = conceptData.conceptIdentifier,
                            facetName = referenceFacetSchema.facetName,
                            reason = "Referenced concept was ${referencedConcept.conceptName}." +
                                    "Must be one these concepts ${possibleConcepts}."
                        )
                        )

                    }
                }
        }
        return if(exceptionList.isEmpty()) null else exceptionList

    }

    private fun validateConceptName(
        schema: SchemaAccess,
        conceptDataEntry: ConceptData
    ): UnknownConceptException? {
        if(!schema.hasConceptName(conceptDataEntry.conceptName)) {
            return UnknownConceptException(conceptDataEntry.conceptName, conceptDataEntry.conceptIdentifier)
        }
        return null
    }

    private fun validateDuplicateConceptIdentifiers(
        allConceptIdentifiers: Set<ConceptIdentifier>,
        conceptDataEntry: ConceptData
    ): DuplicateConceptIdentifierException? {
        if(allConceptIdentifiers.contains(conceptDataEntry.conceptIdentifier)) {
            return DuplicateConceptIdentifierException(conceptDataEntry.conceptName, conceptDataEntry.conceptIdentifier)
        }
        return null;
    }

    private fun validateForObsoletFacets(
        conceptSchema: ConceptSchema,
        conceptData: ConceptData
    ): UnknownFacetNameException? {
        // iterate through all entry facet values to find obsolet ones
        conceptData.getFacetNames().forEach { facetName ->
            if(!conceptSchema.hasFacet(facetName)) {
                return UnknownFacetNameException(
                    concept = conceptData.conceptName,
                    conceptIdentifier = conceptData.conceptIdentifier,
                    facetName = facetName,
                    reason = "Facet with facet name '${facetName}' is not known by the schema. " +
                            "Known facets are: ${conceptSchema.facetNames}"
                )
            }
        }
        return null
    }

    private fun validateForFacetType(
        conceptSchema: ConceptSchema,
        conceptData: ConceptData
    ): List<WrongTypeForFacetValueException>? {
        val exceptionList = mutableListOf<WrongTypeForFacetValueException>()
        conceptSchema.facets.forEach { facetSchema ->
            if(conceptData.hasFacet(facetSchema.facetName)) {
                val facetValues = conceptData.getFacet(facetSchema.facetName)
                val expectedFacetType = facetSchema.facetType
                facetValues.forEach { facetValue ->
                    val actualClass = facetValue::class
                    val isValidType = when(expectedFacetType) {
                        FacetType.TEXT -> actualClass == String::class
                        FacetType.NUMBER -> actualClass == Long::class || actualClass == Int::class
                        FacetType.BOOLEAN -> actualClass == Boolean::class
                        FacetType.REFERENCE -> actualClass == ConceptIdentifier::class
                        FacetType.TEXT_ENUMERATION -> isValidEnumValue(facetValue, facetSchema)
                    }

                    if(!isValidType) {
                        exceptionList.add(
                            WrongTypeForFacetValueException(
                                concept = conceptData.conceptName,
                                conceptIdentifier = conceptData.conceptIdentifier,
                                facetName = facetSchema.facetName,
                                reason = "A facet of type '$expectedFacetType' can not have a value of type '$actualClass' ($facetValue)"
                            )
                        )
                    }
                }
            }
        }
        return if(exceptionList.isEmpty()) null else exceptionList
    }

    private fun isValidEnumValue(enumFacetValue: Any, facetSchema: FacetSchema): Boolean {
        val enumerationType = facetSchema.enumerationType
            ?: throw IllegalStateException("EnumerationType was empty for facet schema $facetSchema")
        return if(enumFacetValue is String) {
            EnumUtil.fromStringToEnum(enumFacetValue, enumerationType) != null
        } else {
            EnumUtil.isEnumerationType(enumFacetValue, enumerationType)
        }
    }

    private fun validateForFacetCardinality(
        schemaConcept: ConceptSchema,
        conceptData: ConceptData
    ): List<WrongCardinalityForFacetValueException>? {
        val exceptionList = mutableListOf<WrongCardinalityForFacetValueException>()
        schemaConcept.facets.forEach { facetSchema ->
            val minimumOccurrences = facetSchema.minimumOccurrences
            val maximumOccurrences = facetSchema.maximumOccurrences
            val numberOfFacetValues = conceptData.getFacet(facetSchema.facetName).size
            if(numberOfFacetValues < minimumOccurrences) {
                exceptionList.add(
                    WrongCardinalityForFacetValueException(
                    concept = conceptData.conceptName,
                    conceptIdentifier = conceptData.conceptIdentifier,
                    facetName = facetSchema.facetName,
                    reason = "The facet must have in minimum $minimumOccurrences entries but had ${numberOfFacetValues}."
                )
                )
            }
            if(numberOfFacetValues > maximumOccurrences) {
                exceptionList.add(
                    WrongCardinalityForFacetValueException(
                    concept = conceptData.conceptName,
                    conceptIdentifier = conceptData.conceptIdentifier,
                    facetName = facetSchema.facetName,
                    reason = "The facet must not have more than $maximumOccurrences entries but had ${numberOfFacetValues}."
                )
                )
            }
        }
        return if(exceptionList.isEmpty()) null else exceptionList
    }
}
