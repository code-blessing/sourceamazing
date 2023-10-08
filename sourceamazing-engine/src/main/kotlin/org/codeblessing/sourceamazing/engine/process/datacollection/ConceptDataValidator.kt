package org.codeblessing.sourceamazing.engine.process.datacollection

import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.datacollection.exceptions.*
import org.codeblessing.sourceamazing.api.process.schema.ConceptSchema
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess

object ConceptDataValidator {

    @Throws(SchemaValidationException::class)
    fun validateSingleEntry(schema: SchemaAccess, conceptData: ConceptData) {
        validateConceptBase(schema, conceptData)

        val schemaConcept = schema.conceptByConceptName(conceptData.conceptName)
        validateParentConceptBase(schemaConcept, conceptData)

        validateForObsoletFacets(schemaConcept, conceptData)
        validateForMissingMandatoryFacets(schemaConcept, conceptData)

        validateForFacetType(schemaConcept, conceptData)
    }

    private fun validateConceptBase(schema: SchemaAccess, conceptData: ConceptData) {
        if(!schema.hasConceptName(conceptData.conceptName)) {
            throw UnknownConceptException(conceptData.conceptName, conceptData.conceptIdentifier)
        }
    }

    private fun validateParentConceptBase(schemaConcept: ConceptSchema, conceptData: ConceptData) {
        if(!isValidParentConcept(schemaConcept, conceptData)) {
            throw InvalidConceptParentException(
                concept = conceptData.conceptName,
                conceptIdentifier = conceptData.conceptIdentifier,
                parentConceptIdentifier = conceptData.parentConceptIdentifier)
        }
    }

    private fun validateForObsoletFacets(schemaConcept: ConceptSchema, conceptData: ConceptData) {
        // iterate through all entry facet values to find obsolet ones
        conceptData.getFacetNames().forEach { facetName ->
            if(!schemaConcept.hasFacet(facetName)) {
                throw UnknownFacetNameException(
                    concept = conceptData.conceptName,
                    conceptIdentifier = conceptData.conceptIdentifier,
                    facetName = facetName,
                    reason = "Facet with facet name '${facetName.name}' is not known by the schema. " +
                            "Known facets are: [${schemaConcept.facetNames.joinToString { it.name }}]"
                )
            }
        }
    }

    private fun validateForMissingMandatoryFacets(schemaConcept: ConceptSchema, conceptData: ConceptData) {
        // iterate through all schema facets to find missing ones
        schemaConcept.facets
            .filter { facetSchema -> facetSchema.mandatory }
            .forEach { facetSchema ->
                if(!conceptData.hasFacet(facetSchema.facetName)) {
                    throw MissingFacetValueException(
                        concept = conceptData.conceptName,
                        conceptIdentifier = conceptData.conceptIdentifier,
                        facetName = facetSchema.facetName,
                    )
                }

                val facetValue = conceptData.getFacet(facetSchema.facetName)

                if(facetValue == null && facetSchema.mandatory) {
                    throw MissingFacetValueException(
                        concept = conceptData.conceptName,
                        conceptIdentifier = conceptData.conceptIdentifier,
                        facetName = facetSchema.facetName,
                    )
                }
        }
    }

    private fun validateForFacetType(schemaConcept: ConceptSchema, conceptData: ConceptData) {
        schemaConcept.facets.forEach { facetSchema ->
            if(conceptData.hasFacet(facetSchema.facetName)) {
                val facetValue = conceptData.getFacet(facetSchema.facetName) ?: return@forEach

                if(!facetSchema.facetType.isCompatibleInputType(facetValue)) {
                    val actualClass = facetValue::class
                    throw WrongTypeForFacetValueException(
                        concept = conceptData.conceptName,
                        conceptIdentifier = conceptData.conceptIdentifier,
                        facetName = facetSchema.facetName,
                        reason = "A facet of type '${facetSchema.facetType}' can not have a value of type '$actualClass'"
                    )
                }
            }
        }

    }

    private fun isValidParentConcept(schemaConcept: ConceptSchema, conceptData: ConceptData): Boolean {
        if(schemaConcept.parentConceptName != null && conceptData.parentConceptIdentifier == null) {
            return false
        }

        return !(schemaConcept.parentConceptName == null && conceptData.parentConceptIdentifier != null)
    }

}
