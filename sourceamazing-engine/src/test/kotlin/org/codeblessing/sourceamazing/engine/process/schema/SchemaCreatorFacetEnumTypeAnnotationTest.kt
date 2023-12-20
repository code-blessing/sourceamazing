package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.EnumFacet
import org.codeblessing.sourceamazing.api.process.schema.annotations.FacetType
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.WrongTypeMalformedSchemaException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaCreatorFacetEnumTypeAnnotationTest {

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
        assertEquals(0, enumFacetSchema.enumerationValues().size)
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
        assertEquals(4, enumFacetSchema.enumerationValues().size)
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.WINTER, enumFacetSchema.enumerationValues()[0])
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.SPRING, enumFacetSchema.enumerationValues()[1])
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.SUMMER, enumFacetSchema.enumerationValues()[2])
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.FALL, enumFacetSchema.enumerationValues()[3])
    }

    @Schema(concepts = [SchemaWithConceptWithInvalidEnumFacet.ConceptClassWithEnumFacet::class])
    private interface SchemaWithConceptWithInvalidEnumFacet {
        @Concept(facets = [
            ConceptClassWithEnumFacet.InvalidEnumFacet::class,
        ])
        interface ConceptClassWithEnumFacet {
            @EnumFacet(enumerationClass = String::class)
            interface InvalidEnumFacet
        }
    }

    @Test
    fun `test invalid enum type on facet should throw an exception`() {
        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithInvalidEnumFacet::class)
        }
    }

    @Schema(concepts = [SchemaWithConceptWithMissingEnumTypeOnFacet.ConceptClassWithEnumFacet::class])
    private interface SchemaWithConceptWithMissingEnumTypeOnFacet {
        @Concept(facets = [
            ConceptClassWithEnumFacet.MissingEnumTypeFacet::class,
        ])
        interface ConceptClassWithEnumFacet {
            @EnumFacet(enumerationClass = Unit::class)
            interface MissingEnumTypeFacet
        }
    }

    @Test
    fun `test enum facet with missing enum type should throw an exception`() {
        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithMissingEnumTypeOnFacet::class)
        }
    }
}