package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.DuplicateFacetMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MissingAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.NotInterfaceMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongAnnotationMalformedSchemaException
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
            @StringFacet
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
            @StringFacet
            interface SchemaAnnotatedFacetClass
        }
    }

    @Test
    fun `test create facet class with a schema annotation should throw an exception`() {
        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaAndConceptWithFacetClassHavingSchemaAnnotation::class)
        }
    }

    @Schema(concepts = [SchemaAndConceptWithFacetClassHavingMultipleFacetAnnotation.ConceptClassWithMultipleAnnotatedFacet::class])
    private interface SchemaAndConceptWithFacetClassHavingMultipleFacetAnnotation {
        @Concept(facets = [ConceptClassWithMultipleAnnotatedFacet.MultipleAnnotatedFacetClass::class])
        interface ConceptClassWithMultipleAnnotatedFacet {
            @StringFacet
            @IntFacet
            interface MultipleAnnotatedFacetClass
        }
    }

    @Test
    fun `test create facet with multiple facet annotations should throw an exception`() {
        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(
                SchemaAndConceptWithFacetClassHavingMultipleFacetAnnotation::class
            )
        }
    }

    @Schema(concepts = [SchemaAndConceptWithFacetClassHavingConceptAnnotation.ConceptClassWithConceptAnnotatedFacet::class])
    private interface SchemaAndConceptWithFacetClassHavingConceptAnnotation {
        @Concept(facets = [ConceptClassWithConceptAnnotatedFacet.ConceptAnnotatedFacetClass::class])
        interface ConceptClassWithConceptAnnotatedFacet {
            @Concept(facets = [])
            @StringFacet
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
            @StringFacet
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

}