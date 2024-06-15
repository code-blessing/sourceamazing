package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SchemaCreatorTest {

    @Schema(concepts = [])
    private interface EmptySchemaDefinitionClass

    @Test
    fun `test create an empty schema from an empty schema interface`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(EmptySchemaDefinitionClass::class)
        assertEquals(0, schema.numberOfConcepts())
    }

    @Schema(concepts = [SchemaWithEmptyConceptClass.EmptyConceptClass::class])
    private interface SchemaWithEmptyConceptClass {

        @Concept(facets = [])
        interface EmptyConceptClass
    }

    @Test
    fun `test create an schema with an empty concept class`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithEmptyConceptClass::class)
        assertEquals(1, schema.numberOfConcepts())
    }

    @Schema(concepts = [SchemaWithConceptWithTextFacetClass.ConceptClassWithTextFacet::class])
    private interface SchemaWithConceptWithTextFacetClass {
        @Concept(facets = [ConceptClassWithTextFacet.TextFacetClass::class])
        interface ConceptClassWithTextFacet {
            @StringFacet
            interface TextFacetClass
        }
    }

    @Test
    fun `test create an schema with concept class having a text facet`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithTextFacetClass::class)
        assertEquals(1, schema.numberOfConcepts())
        val concept = schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithTextFacetClass.ConceptClassWithTextFacet::class))
        assertTrue(concept.hasFacet(FacetName.of(SchemaWithConceptWithTextFacetClass.ConceptClassWithTextFacet.TextFacetClass::class)))
    }

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
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithCorrectCardinalityFacets::class)
        val conceptSchema = schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithCorrectCardinalityFacets.ConceptWithFacets::class))

        assertEquals(0, conceptSchema.facets[0].minimumOccurrences)
        assertEquals(1, conceptSchema.facets[0].maximumOccurrences)

        assertEquals(1, conceptSchema.facets[1].minimumOccurrences)
        assertEquals(1, conceptSchema.facets[1].maximumOccurrences)

        assertEquals(2, conceptSchema.facets[2].minimumOccurrences)
        assertEquals(5, conceptSchema.facets[2].maximumOccurrences)
    }

    @Schema(concepts = [SchemaWithConceptWithEmptyEnumFacet.ConceptClassWithEnumFacet::class])
    private interface SchemaWithConceptWithEmptyEnumFacet {
        @Concept(facets = [
            ConceptClassWithEnumFacet.MyEnumFacet::class,
        ])
        interface ConceptClassWithEnumFacet {
            @EnumFacet(enumerationClass = EmptyEnumeration::class)
            interface MyEnumFacet
        }

        enum class EmptyEnumeration
    }

    @Test
    fun `test concept having an empty enumeration facet should not throw an exception`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithEmptyEnumFacet::class)

        val conceptSchema = schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithEmptyEnumFacet.ConceptClassWithEnumFacet::class))
        val enumFacetName = FacetName.of(SchemaWithConceptWithEmptyEnumFacet.ConceptClassWithEnumFacet.MyEnumFacet::class)
        val enumFacetSchema = conceptSchema.facetByName(enumFacetName)
        assertEquals(enumFacetName, enumFacetSchema.facetName)
        assertEquals(FacetType.TEXT_ENUMERATION, enumFacetSchema.facetType)
        assertEquals(SchemaWithConceptWithEmptyEnumFacet.EmptyEnumeration::class, enumFacetSchema.enumerationType)
        assertEquals(0, enumFacetSchema.enumerationValues.size)
    }


    @Schema(concepts = [SchemaWithConceptWithPrimitiveFacetClasses.ConceptClassWithFacets::class])
    private interface SchemaWithConceptWithPrimitiveFacetClasses {
        @Concept(facets = [
            ConceptClassWithFacets.TextFacetClass::class,
            ConceptClassWithFacets.BooleanFacetClass::class,
            ConceptClassWithFacets.NumberFacetClass::class,
        ])
        interface ConceptClassWithFacets {
            @StringFacet
            interface TextFacetClass
            @BooleanFacet
            interface BooleanFacetClass
            @IntFacet
            interface NumberFacetClass
        }
    }

    @Test
    fun `test concept having three primitive type facet`() {
        val schema =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithPrimitiveFacetClasses::class)
        assertEquals(1, schema.numberOfConcepts())
        val conceptSchema = schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithPrimitiveFacetClasses.ConceptClassWithFacets::class))
        assertEquals(3, conceptSchema.facets.size)

        val textFacetName = FacetName.of(SchemaWithConceptWithPrimitiveFacetClasses.ConceptClassWithFacets.TextFacetClass::class)
        val booleanFacetName = FacetName.of(SchemaWithConceptWithPrimitiveFacetClasses.ConceptClassWithFacets.BooleanFacetClass::class)
        val numberFacetName = FacetName.of(SchemaWithConceptWithPrimitiveFacetClasses.ConceptClassWithFacets.NumberFacetClass::class)

        assertEquals(textFacetName, conceptSchema.facets[0].facetName)
        assertEquals(booleanFacetName, conceptSchema.facets[1].facetName)
        assertEquals(numberFacetName, conceptSchema.facets[2].facetName)

        assertEquals(FacetType.TEXT, conceptSchema.facets[0].facetType)
        assertEquals(FacetType.BOOLEAN, conceptSchema.facets[1].facetType)
        assertEquals(FacetType.NUMBER, conceptSchema.facets[2].facetType)

        assertNull(conceptSchema.facets[0].enumerationType)
        assertNull(conceptSchema.facets[1].enumerationType)
        assertNull(conceptSchema.facets[2].enumerationType)

        assertEquals(0, conceptSchema.facets[0].enumerationValues.size)
        assertEquals(0, conceptSchema.facets[1].enumerationValues.size)
        assertEquals(0, conceptSchema.facets[2].enumerationValues.size)
    }

    @Schema(concepts = [SchemaWithConceptWithEnumFacet.ConceptClassWithEnumFacet::class])
    private interface SchemaWithConceptWithEnumFacet {
        @Concept(facets = [
            ConceptClassWithEnumFacet.MyEnumFacet::class,
        ])
        interface ConceptClassWithEnumFacet {
            @EnumFacet(enumerationClass = SeasonEnumeration::class)
            interface MyEnumFacet
        }

        enum class SeasonEnumeration {
            WINTER,
            SPRING,
            SUMMER,
            FALL,
        }
    }

    @Test
    fun `test concept having a enumeration facet`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithEnumFacet::class)

        val conceptSchema = schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithEnumFacet.ConceptClassWithEnumFacet::class))
        val enumFacetName = FacetName.of(SchemaWithConceptWithEnumFacet.ConceptClassWithEnumFacet.MyEnumFacet::class)
        val enumFacetSchema = conceptSchema.facetByName(enumFacetName)
        assertEquals(enumFacetName, enumFacetSchema.facetName)
        assertEquals(FacetType.TEXT_ENUMERATION, enumFacetSchema.facetType)
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration::class, enumFacetSchema.enumerationType)
        assertEquals(4, enumFacetSchema.enumerationValues.size)
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.WINTER, enumFacetSchema.enumerationValues[0])
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.SPRING, enumFacetSchema.enumerationValues[1])
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.SUMMER, enumFacetSchema.enumerationValues[2])
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.FALL, enumFacetSchema.enumerationValues[3])
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
}