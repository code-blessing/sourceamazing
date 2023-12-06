package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.api.process.schema.annotations.FacetType
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.MalformedSchemaException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaCreatorFacetCardinalityAnnotationTest {

    @Schema(concepts = [SchemaWithConceptWithCorrectCardinalityFacetsClasses.ConceptClassWithFacets::class])
    private interface SchemaWithConceptWithCorrectCardinalityFacetsClasses {
        @Concept(facets = [
            ConceptClassWithFacets.TextFacetClass::class,
            ConceptClassWithFacets.BooleanFacetClass::class,
            ConceptClassWithFacets.NumberFacetClass::class,
        ])
        interface ConceptClassWithFacets {
            @Facet(FacetType.TEXT, minimumOccurrences = 0, maximumOccurrences = 1)
            interface TextFacetClass
            @Facet(FacetType.BOOLEAN, minimumOccurrences = 1, maximumOccurrences = 1)
            interface BooleanFacetClass
            @Facet(FacetType.NUMBER, minimumOccurrences = 2, maximumOccurrences = 5)
            interface NumberFacetClass
        }
    }

    @Test
    fun `test concept having three facets with correct cardinalities`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(
            SchemaWithConceptWithCorrectCardinalityFacetsClasses::class)
        val conceptSchema = schema.conceptByConceptName(ConceptName.of(
            SchemaWithConceptWithCorrectCardinalityFacetsClasses.ConceptClassWithFacets::class))

        assertEquals(0, conceptSchema.facets[0].minimumOccurrences)
        assertEquals(1, conceptSchema.facets[0].maximumOccurrences)

        assertEquals(1, conceptSchema.facets[1].minimumOccurrences)
        assertEquals(1, conceptSchema.facets[1].maximumOccurrences)

        assertEquals(2, conceptSchema.facets[2].minimumOccurrences)
        assertEquals(5, conceptSchema.facets[2].maximumOccurrences)
    }


    @Schema(concepts = [SchemaWithConceptWithNegativeCardinalityFacetsClasses.ConceptClassWithFacet::class])
    private interface SchemaWithConceptWithNegativeCardinalityFacetsClasses {
        @Concept(facets = [ConceptClassWithFacet.FacetClass::class])
        interface ConceptClassWithFacet {
            @Facet(FacetType.TEXT, minimumOccurrences = -1, maximumOccurrences = 1)
            interface FacetClass
        }
    }

    @Test
    fun `test negative cardinality on facet should throw an exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithNegativeCardinalityFacetsClasses::class)
        }
    }

    @Schema(concepts = [SchemaWithConceptWithSwappedCardinalityFacetClass.ConceptClassWithFacet::class])
    private interface SchemaWithConceptWithSwappedCardinalityFacetClass {
        @Concept(facets = [ConceptClassWithFacet.FacetClass::class])
        interface ConceptClassWithFacet {
            @Facet(FacetType.TEXT, minimumOccurrences = 3, maximumOccurrences = 2)
            interface FacetClass
        }
    }

    @Test
    fun `test min cardinality is greater than maximum cardinality on facet should throw an exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithSwappedCardinalityFacetClass::class)
        }
    }

}