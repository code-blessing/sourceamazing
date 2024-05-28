package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class SchemaCreatorConceptAnnotationTest {

    @Schema(concepts = [SchemaWithUnannotatedConceptClass.UnannotatedConceptClass::class])
    private interface SchemaWithUnannotatedConceptClass {
        interface UnannotatedConceptClass
    }

    @Test
    fun `test unannotated concept class should throw an exception`() {
        assertThrows(MissingAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithUnannotatedConceptClass::class)
        }
    }

    @Schema(concepts = [SchemaWithNonInterfaceConceptClass.NonInterfaceConceptClass::class])
    private interface SchemaWithNonInterfaceConceptClass {

        @Concept(facets = [])
        class NonInterfaceConceptClass
    }

    @Test
    fun `test non-interface concept class should throw an exception`() {
        assertThrows(NotInterfaceMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithNonInterfaceConceptClass::class)
        }
    }

    @Schema(concepts = [SchemaWithConceptClassHavingSchemaAnnotation.ConceptClassWithSchemaAnnotation::class])
    private interface SchemaWithConceptClassHavingSchemaAnnotation {

        @Concept(facets = [])
        @Schema(concepts = [])
        interface ConceptClassWithSchemaAnnotation
    }

    @Test
    fun `test create concept class with a schema annotation should throw an exception`() {
        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptClassHavingSchemaAnnotation::class)
        }
    }

    @Schema(concepts = [SchemaWithConceptClassHavingFacetAnnotation.ConceptClassWithFacetAnnotation::class])
    private interface SchemaWithConceptClassHavingFacetAnnotation {

        @Concept(facets = [])
        @StringFacet
        interface ConceptClassWithFacetAnnotation
    }

    @Test
    fun `test concept class with a facet annotation should throw an exception`() {
        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptClassHavingFacetAnnotation::class)
        }
    }

    @Schema(concepts = [
        SchemaWithDuplicateConceptClasses.DuplicateConceptClass::class,
        SchemaWithDuplicateConceptClasses.DuplicateConceptClass::class,
    ])
    private interface SchemaWithDuplicateConceptClasses {

        @Concept(facets = [])
        interface DuplicateConceptClass
    }

    @Test
    fun `test duplicate concept classes should throw an exception`() {
        assertThrows(DuplicateConceptMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithDuplicateConceptClasses::class)
        }
    }

    @Schema(concepts = [
        SchemaWithTwoConceptAnnotationsInHierarchyClasses.ChildConceptClass::class,
    ])
    private interface SchemaWithTwoConceptAnnotationsInHierarchyClasses {

        @Concept(facets = [])
        interface ParentConceptClass
        @Concept(facets = [])
        interface ChildConceptClass: ParentConceptClass
    }

    @Test
    @Disabled("Not prevented currently")
    fun `test concept with two concept annotations in hierarchy should throw an exception`() {
        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithTwoConceptAnnotationsInHierarchyClasses::class)
        }
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

}