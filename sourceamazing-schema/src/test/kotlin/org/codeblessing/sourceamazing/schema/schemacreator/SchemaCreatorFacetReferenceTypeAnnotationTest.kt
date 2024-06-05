package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongTypeMalformedSchemaException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SchemaCreatorFacetReferenceTypeAnnotationTest {
    @Schema(concepts = [SchemaWithConceptWithEmptyReferenceFacet.ConceptClassWithEnumFacet::class])
    private interface SchemaWithConceptWithEmptyReferenceFacet {
        @Concept(facets = [
            ConceptClassWithEnumFacet.MyReferenceFacet::class,
        ])
        interface ConceptClassWithEnumFacet {
            @ReferenceFacet(referencedConcepts = [])
            interface MyReferenceFacet
        }
    }

    @Test
    fun `test concept having an empty reference facet should throw an exception`() {
        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithEmptyReferenceFacet::class)
        }
    }


    @Schema(concepts = [
        SchemaWithConceptWithReferenceFacet.ConceptClassWithReferenceFacet::class,
        SchemaWithConceptWithReferenceFacet.OtherConcept::class,
    ])
    private interface SchemaWithConceptWithReferenceFacet {
        @Concept(facets = [
            ConceptClassWithReferenceFacet.MyReferenceFacet::class,
        ])
        interface ConceptClassWithReferenceFacet {
            @ReferenceFacet(referencedConcepts = [OtherConcept::class])
            interface MyReferenceFacet
        }

        @Concept(facets = [])
        interface OtherConcept
    }

    @Test
    fun `test concept having a reference facet referencing one concept`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithReferenceFacet::class)

        val conceptSchema = schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithReferenceFacet.ConceptClassWithReferenceFacet::class))
        val referenceFacetName = FacetName.of(SchemaWithConceptWithReferenceFacet.ConceptClassWithReferenceFacet.MyReferenceFacet::class)
        val otherReferencedConceptName = ConceptName.of(SchemaWithConceptWithReferenceFacet.OtherConcept::class)
        val referenceFacetSchema = conceptSchema.facetByName(referenceFacetName)
        assertEquals(referenceFacetName, referenceFacetSchema.facetName)
        assertEquals(FacetType.REFERENCE, referenceFacetSchema.facetType)
        assertEquals(1, referenceFacetSchema.referencingConcepts.size)
        assertEquals(otherReferencedConceptName, referenceFacetSchema.referencingConcepts.first())
    }

    @Schema(concepts = [
        SchemaWithConceptWithReferenceFacetToMultipleConcepts.ConceptClassWithReferenceFacet::class,
        SchemaWithConceptWithReferenceFacetToMultipleConcepts.OtherConcept::class,
        SchemaWithConceptWithReferenceFacetToMultipleConcepts.AndAnotherConcept::class,
        SchemaWithConceptWithReferenceFacetToMultipleConcepts.AndJustOneAnotherConcept::class,
    ])
    private interface SchemaWithConceptWithReferenceFacetToMultipleConcepts {
        @Concept(facets = [
            ConceptClassWithReferenceFacet.MyReferenceFacet::class,
        ])
        interface ConceptClassWithReferenceFacet {
            @ReferenceFacet(
                referencedConcepts = [OtherConcept::class, AndAnotherConcept::class, AndJustOneAnotherConcept::class])
            interface MyReferenceFacet
        }

        @Concept(facets = [])
        interface OtherConcept

        @Concept(facets = [])
        interface AndAnotherConcept

        @Concept(facets = [])
        interface AndJustOneAnotherConcept

    }

    @Test
    fun `test concept having a reference facet referencing multiple concept`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(
            SchemaWithConceptWithReferenceFacetToMultipleConcepts::class
        )

        val conceptSchema = schema.conceptByConceptName(
            ConceptName.of(
            SchemaWithConceptWithReferenceFacetToMultipleConcepts.ConceptClassWithReferenceFacet::class))
        val referenceFacetName = FacetName.of(SchemaWithConceptWithReferenceFacetToMultipleConcepts.ConceptClassWithReferenceFacet.MyReferenceFacet::class)
        val referenceFacetSchema = conceptSchema.facetByName(referenceFacetName)

        val referencedConceptName1 = ConceptName.of(SchemaWithConceptWithReferenceFacetToMultipleConcepts.OtherConcept::class)
        val referencedConceptName2 = ConceptName.of(SchemaWithConceptWithReferenceFacetToMultipleConcepts.AndAnotherConcept::class)
        val referencedConceptName3 = ConceptName.of(SchemaWithConceptWithReferenceFacetToMultipleConcepts.AndJustOneAnotherConcept::class)

        assertEquals(referenceFacetName, referenceFacetSchema.facetName)
        assertEquals(FacetType.REFERENCE, referenceFacetSchema.facetType)
        assertEquals(3, referenceFacetSchema.referencingConcepts.size)
        assertTrue(referenceFacetSchema.referencingConcepts.contains(referencedConceptName1))
        assertTrue(referenceFacetSchema.referencingConcepts.contains(referencedConceptName2))
        assertTrue(referenceFacetSchema.referencingConcepts.contains(referencedConceptName3))
    }

    @Schema(concepts = [SchemaWithConceptWithUnknownReferencedConceptFacet.ConceptClassWithReferenceFacet::class])
    private interface SchemaWithConceptWithUnknownReferencedConceptFacet {
        @Concept(facets = [
            ConceptClassWithReferenceFacet.ReferenceFacetToUnknownConcept::class,
        ])
        interface ConceptClassWithReferenceFacet {
            @ReferenceFacet(referencedConcepts = [OtherConcept::class])
            interface ReferenceFacetToUnknownConcept
        }

        @Concept(facets = [])
        interface OtherConcept // but not listed in schema

    }

    @Test
    fun `test reference facet to unknown concept should throw an exception`() {
        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithUnknownReferencedConceptFacet::class)
        }
    }

    @Schema(concepts = [SchemaWithConceptWithMissingReferenceTypeOnFacet.ConceptClassWithEnumFacet::class])
    private interface SchemaWithConceptWithMissingReferenceTypeOnFacet {
        @Concept(facets = [
            ConceptClassWithEnumFacet.MissingReferenceTypeFacet::class,
        ])
        interface ConceptClassWithEnumFacet {
            @ReferenceFacet(referencedConcepts = [])
            interface MissingReferenceTypeFacet
        }
    }

    @Test
    fun `test reference facet with missing reference type should throw an exception`() {
        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithMissingReferenceTypeOnFacet::class)
        }
    }

    @Schema(concepts = [SchemaWithConceptWithReferenceToNonConceptClass.ConceptClassWithReferenceFacet::class])
    private interface SchemaWithConceptWithReferenceToNonConceptClass {
        @Concept(facets = [
            ConceptClassWithReferenceFacet.ReferenceFacetToNonConceptClass::class,
        ])
        interface ConceptClassWithReferenceFacet {
            @ReferenceFacet(referencedConcepts = [OtherConceptWithoutConceptAnnotation::class])
            interface ReferenceFacetToNonConceptClass
        }

        class OtherConceptWithoutConceptAnnotation
    }

    @Test
    fun `test reference facet to a non-concept class should throw an exception`() {
        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithReferenceToNonConceptClass::class)
        }
    }
}