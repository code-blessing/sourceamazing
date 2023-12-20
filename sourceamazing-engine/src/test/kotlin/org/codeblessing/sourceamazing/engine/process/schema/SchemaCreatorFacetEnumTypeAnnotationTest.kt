package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.api.process.schema.annotations.FacetType
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.WrongTypeMalformedSchemaException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaCreatorFacetEnumTypeAnnotationTest {

    @Schema(concepts = [SchemaWithConceptWithEmptyEnumFacet.ConceptClassWithEnumFacet::class])
    private interface SchemaWithConceptWithEmptyEnumFacet {
        @Concept(facets = [
            ConceptClassWithEnumFacet.EnumFacet::class,
        ])
        interface ConceptClassWithEnumFacet {
            @Facet(FacetType.TEXT_ENUMERATION, enumerationClass = EmptyEnumeration::class)
            interface EnumFacet
        }

        enum class EmptyEnumeration
    }

    @Test
    fun `test concept having an empty enumeration facet should not throw an exception`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithEmptyEnumFacet::class)

        val conceptSchema = schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithEmptyEnumFacet.ConceptClassWithEnumFacet::class))
        val enumFacetName = FacetName.of(SchemaWithConceptWithEmptyEnumFacet.ConceptClassWithEnumFacet.EnumFacet::class)
        val enumFacetSchema = conceptSchema.facetByName(enumFacetName)
        assertEquals(enumFacetName, enumFacetSchema.facetName)
        assertEquals(FacetType.TEXT_ENUMERATION, enumFacetSchema.facetType)
        assertEquals(SchemaWithConceptWithEmptyEnumFacet.EmptyEnumeration::class, enumFacetSchema.enumerationType)
        assertEquals(0, enumFacetSchema.enumerationValues().size)
    }


    @Schema(concepts = [SchemaWithConceptWithEnumFacet.ConceptClassWithEnumFacet::class])
    private interface SchemaWithConceptWithEnumFacet {
        @Concept(facets = [
            ConceptClassWithEnumFacet.EnumFacet::class,
        ])
        interface ConceptClassWithEnumFacet {
            @Facet(FacetType.TEXT_ENUMERATION, enumerationClass = SeasonEnumeration::class)
            interface EnumFacet
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
        val enumFacetName = FacetName.of(SchemaWithConceptWithEnumFacet.ConceptClassWithEnumFacet.EnumFacet::class)
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
            @Facet(FacetType.TEXT_ENUMERATION, enumerationClass = String::class)
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
            @Facet(FacetType.TEXT_ENUMERATION)
            interface MissingEnumTypeFacet
        }
    }

    @Test
    fun `test enum facet with missing enum type should throw an exception`() {
        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithMissingEnumTypeOnFacet::class)
        }
    }

    @Schema(concepts = [SchemaWithConceptWithNonEnumTypeButEnumFacet.ConceptClassWithEnumFacet::class])
    private interface SchemaWithConceptWithNonEnumTypeButEnumFacet {
        @Concept(facets = [
            ConceptClassWithEnumFacet.InvalidEnumFacet::class,
        ])
        interface ConceptClassWithEnumFacet {
            @Facet(FacetType.NUMBER, enumerationClass = AnotherEnumeration::class)
            interface InvalidEnumFacet
        }

        enum class AnotherEnumeration { X,Y,Z }
    }

    @Test
    fun `test non-enum type with a enum should throw an exception`() {
        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithNonEnumTypeButEnumFacet::class)
        }
    }
}