package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaCreatorFacetCardinalityAnnotationTest {

    @Schema(concepts = [SchemaWithConceptWithCorrectCardinalityFacets.ConceptWithFacets::class])
    private interface SchemaWithConceptWithCorrectCardinalityFacets {
        @Concept(facets = [
            ConceptWithFacets.TextFacet::class,
            ConceptWithFacets.BoolFacet::class,
            ConceptWithFacets.NumberFacet::class,
        ])
        interface ConceptWithFacets {
            @StringFacet(minimumOccurrences = 0, maximumOccurrences = 1)
            interface TextFacet
            @BooleanFacet(minimumOccurrences = 1, maximumOccurrences = 1)
            interface BoolFacet
            @IntFacet(minimumOccurrences = 2, maximumOccurrences = 5)
            interface NumberFacet
        }
    }

    @Test
    fun `test concept having three facets with correct cardinalities`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(
            SchemaWithConceptWithCorrectCardinalityFacets::class
        )
        val conceptSchema = schema.conceptByConceptName(
            ConceptName.of(
            SchemaWithConceptWithCorrectCardinalityFacets.ConceptWithFacets::class))

        assertEquals(0, conceptSchema.facets[0].minimumOccurrences)
        assertEquals(1, conceptSchema.facets[0].maximumOccurrences)

        assertEquals(1, conceptSchema.facets[1].minimumOccurrences)
        assertEquals(1, conceptSchema.facets[1].maximumOccurrences)

        assertEquals(2, conceptSchema.facets[2].minimumOccurrences)
        assertEquals(5, conceptSchema.facets[2].maximumOccurrences)
    }
}