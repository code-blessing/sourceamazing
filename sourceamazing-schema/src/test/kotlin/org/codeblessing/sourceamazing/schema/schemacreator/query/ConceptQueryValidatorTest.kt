package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Test

class ConceptQueryValidatorTest {

    @Schema(concepts = [ SchemaWithoutQueryMethods.ConceptWithoutQueryMethods::class])
    private interface SchemaWithoutQueryMethods {
        @Concept(facets = [])
        interface ConceptWithoutQueryMethods
    }

    @Test
    fun `test concept without accessor method should return without exception`() {
        ConceptQueryValidator.validateAccessorMethodsOfConceptClass(SchemaWithoutQueryMethods.ConceptWithoutQueryMethods::class)
    }

    @Schema(concepts = [ SchemaWithConceptWithValidAnnotatedQueryMethods.ConceptWithValidAnnotatedQueryMethods::class ])
    private interface SchemaWithConceptWithValidAnnotatedQueryMethods {
        @Concept(facets = [ConceptWithValidAnnotatedQueryMethods.OneFacet::class])
        interface ConceptWithValidAnnotatedQueryMethods {

            @StringFacet
            interface OneFacet

            @Suppress("Unused")
            @QueryFacetValue(OneFacet::class)
            fun getFacetValue(): List<Any>
        }
    }

    @Test
    fun `test concept with a valid annotated method should return without exception`() {
        ConceptQueryValidator.validateAccessorMethodsOfConceptClass(SchemaWithConceptWithValidAnnotatedQueryMethods.ConceptWithValidAnnotatedQueryMethods::class)
    }
}