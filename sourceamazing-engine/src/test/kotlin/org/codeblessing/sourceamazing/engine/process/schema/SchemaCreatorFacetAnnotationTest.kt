package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.api.process.schema.annotations.FacetType
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.DuplicateFacetMalformedSchemaException
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.MissingAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.NotInterfaceMalformedSchemaException
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.WrongAnnotationMalformedSchemaException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SchemaCreatorFacetAnnotationTest {

    @Schema(concepts = [SchemaAndConceptWithUnannotatedFacetClass.ConceptClassWithUnannotatedFacet::class])
    private interface SchemaAndConceptWithUnannotatedFacetClass {
        @Concept(facets = [ConceptClassWithUnannotatedFacet.UnannotatedFacetClass::class])
        interface ConceptClassWithUnannotatedFacet {
            interface UnannotatedFacetClass
        }
    }

    @Test
    fun `test create a concept with an unannotated facet should throw an exception`() {
        assertThrows(MissingAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaAndConceptWithUnannotatedFacetClass::class)
        }
    }

    @Schema(concepts = [SchemaAndConceptWithNonInterfaceFacetClass.ConceptClassWithNonInterfaceFacet::class])
    private interface SchemaAndConceptWithNonInterfaceFacetClass {
        @Concept(facets = [ConceptClassWithNonInterfaceFacet.NonInterfaceFacetClass::class])
        interface ConceptClassWithNonInterfaceFacet {
            @Facet(FacetType.TEXT)
            class NonInterfaceFacetClass
        }
    }

    @Test
    fun `test create a concept with an non-interface facet should throw an exception`() {
        assertThrows(NotInterfaceMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaAndConceptWithNonInterfaceFacetClass::class)
        }
    }

    @Schema(concepts = [SchemaAndConceptWithFacetClassHavingSchemaAnnotation.ConceptClassWithSchemaAnnotatedFacet::class])
    private interface SchemaAndConceptWithFacetClassHavingSchemaAnnotation {
        @Concept(facets = [ConceptClassWithSchemaAnnotatedFacet.SchemaAnnotatedFacetClass::class])
        interface ConceptClassWithSchemaAnnotatedFacet {
            @Schema(concepts = [])
            @Facet(FacetType.TEXT)
            interface SchemaAnnotatedFacetClass
        }
    }

    @Test
    fun `test create facet class with a schema annotation should throw an exception`() {
        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaAndConceptWithFacetClassHavingSchemaAnnotation::class)
        }
    }

    @Schema(concepts = [SchemaAndConceptWithFacetClassHavingConceptAnnotation.ConceptClassWithConceptAnnotatedFacet::class])
    private interface SchemaAndConceptWithFacetClassHavingConceptAnnotation {
        @Concept(facets = [ConceptClassWithConceptAnnotatedFacet.ConceptAnnotatedFacetClass::class])
        interface ConceptClassWithConceptAnnotatedFacet {
            @Concept(facets = [])
            @Facet(FacetType.TEXT)
            interface ConceptAnnotatedFacetClass
        }
    }

    @Test
    fun `test create facet class with a concept annotation should throw an exception`() {
        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaAndConceptWithFacetClassHavingConceptAnnotation::class)
        }
    }

    @Schema(concepts = [SchemaAndConceptWithDuplicateFacetClass.ConceptClassWithDuplicateFacets::class])
    private interface SchemaAndConceptWithDuplicateFacetClass {
        @Concept(facets = [
            ConceptClassWithDuplicateFacets.OneTextFacetClass::class,
            ConceptClassWithDuplicateFacets.OneTextFacetClass::class,
        ])
        interface ConceptClassWithDuplicateFacets {
            @Facet(FacetType.TEXT)
            interface OneTextFacetClass
        }
    }

    @Test
    fun `test duplicate facet class within a concept should throw an exception`() {
        assertThrows(DuplicateFacetMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaAndConceptWithDuplicateFacetClass::class)
        }
    }

    @Schema(concepts = [SchemaWithConceptWithTextFacetClass.ConceptClassWithTextFacet::class])
    private interface SchemaWithConceptWithTextFacetClass {
        @Concept(facets = [ConceptClassWithTextFacet.TextFacetClass::class])
        interface ConceptClassWithTextFacet {
            @Facet(FacetType.TEXT)
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

}