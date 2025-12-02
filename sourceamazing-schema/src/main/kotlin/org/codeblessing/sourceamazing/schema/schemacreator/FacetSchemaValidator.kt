package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.WrongFacetSchemaException
import org.codeblessing.sourceamazing.utils.type.isEnum
import org.codeblessing.sourceamazing.utils.type.isPrivate

object FacetSchemaValidator {
    fun validatedFacetSchema(facetSchema: FacetSchema, conceptName: ConceptName) {
        val facetName: FacetName = facetSchema.facetName

        val minimumOccurrences = facetSchema.minimumOccurrences
        val maximumOccurrences = facetSchema.maximumOccurrences

        when (facetSchema) {
            is EnumFacetSchema -> {
                val enumerationType = facetSchema.enumerationType
                if (!enumerationType.isEnum) {
                    throw WrongFacetSchemaException(
                        SchemaErrorCode.FACET_ENUM_INVALID,
                        facetName,
                        conceptName,
                        enumerationType,
                    )
                }

                if (enumerationType.isPrivate) {
                    throw WrongFacetSchemaException(
                        SchemaErrorCode.FACET_ENUM_HAS_PRIVATE_MODIFIER,
                        facetName,
                        conceptName,
                        enumerationType,
                    )
                }
            }
            is ReferenceFacetSchema -> {
                if (facetSchema.referencingConcepts.isEmpty()) {
                    throw WrongFacetSchemaException(
                        SchemaErrorCode.FACET_REFERENCE_EMPTY_CONCEPT_LIST,
                        facetName,
                        conceptName,
                    )
                }
            }
            else -> {
                // no type specific validation
            }
        }

        if (minimumOccurrences < 0 || maximumOccurrences < 0) {
            throw WrongFacetSchemaException(
                SchemaErrorCode.NO_NEGATIVE_FACET_CARDINALITIES,
                facetName,
                conceptName,
                minimumOccurrences,
                maximumOccurrences,
            )
        }

        if (minimumOccurrences > maximumOccurrences) {
            throw WrongFacetSchemaException(
                SchemaErrorCode.WRONG_FACET_CARDINALITIES,
                facetName,
                conceptName,
                minimumOccurrences,
                maximumOccurrences,
            )
        }
    }
}
