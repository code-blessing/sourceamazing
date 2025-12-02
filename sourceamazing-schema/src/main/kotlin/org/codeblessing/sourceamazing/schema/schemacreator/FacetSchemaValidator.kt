package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.schemaaccess.EnumFacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.FacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.ReferenceFacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.WrongFacetSchemaException
import org.codeblessing.sourceamazing.utils.type.isEnum
import org.codeblessing.sourceamazing.utils.type.isPrivate

object FacetSchemaValidator {
    fun validatedFacetSchema(facetSchema: FacetSchema) {
        when (facetSchema) {
            is EnumFacetSchema -> {
                checkIsEnumType(facetSchema)
                checkIsNotPrivate(facetSchema)
            }
            is ReferenceFacetSchema -> {
                checkHasReferencingConcepts(facetSchema)
            }
            else -> {
                // no type specific validation
            }
        }

        checkMinimumAndMaximumOccurrencesNotNegative(facetSchema)
        checkMinimumOccurrencesLowerOrEqualToMaximumOccurrences(facetSchema)
    }

    private fun checkIsEnumType(facetSchema: EnumFacetSchema) {
        if (!facetSchema.enumerationClass.isEnum) {
            throw WrongFacetSchemaException(
                SchemaErrorCode.FACET_ENUM_INVALID,
                facetSchema.facetName,
                facetSchema.conceptName,
                facetSchema.enumerationClass,
            )
        }
    }

    private fun checkIsNotPrivate(facetSchema: EnumFacetSchema) {
        if (facetSchema.enumerationClass.isPrivate) {
            throw WrongFacetSchemaException(
                SchemaErrorCode.FACET_ENUM_HAS_PRIVATE_MODIFIER,
                facetSchema.facetName,
                facetSchema.conceptName,
                facetSchema.enumerationClass,
            )
        }
    }

    private fun checkHasReferencingConcepts(facetSchema: ReferenceFacetSchema) {
        if (facetSchema.referencingConcepts.isEmpty()) {
            throw WrongFacetSchemaException(
                SchemaErrorCode.FACET_REFERENCE_EMPTY_CONCEPT_LIST,
                facetSchema.facetName,
                facetSchema.conceptName,
            )
        }
    }

    private fun checkMinimumOccurrencesLowerOrEqualToMaximumOccurrences(facetSchema: FacetSchema) {
        val minimumOccurrences = facetSchema.minimumOccurrences
        val maximumOccurrences = facetSchema.maximumOccurrences

        if (minimumOccurrences > maximumOccurrences) {
            throw WrongFacetSchemaException(
                SchemaErrorCode.WRONG_FACET_CARDINALITIES,
                facetSchema.facetName,
                facetSchema.conceptName,
                minimumOccurrences,
                maximumOccurrences,
            )
        }
    }

    private fun checkMinimumAndMaximumOccurrencesNotNegative(facetSchema: FacetSchema) {
        val minimumOccurrences = facetSchema.minimumOccurrences
        val maximumOccurrences = facetSchema.maximumOccurrences

        if (minimumOccurrences < 0 || maximumOccurrences < 0) {
            throw WrongFacetSchemaException(
                SchemaErrorCode.NO_NEGATIVE_FACET_CARDINALITIES,
                facetSchema.facetName,
                facetSchema.conceptName,
                minimumOccurrences,
                maximumOccurrences,
            )
        }
    }
}
