package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MissingAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.NotInterfaceMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongAnnotationMalformedSchemaException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class SchemaCreatorSchemaAnnotationTest {

    private interface UnannotatedSchemaDefinitionClass

    @Test
    fun `test unannotated schema class should throw an exception`() {
        assertThrows(MissingAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(UnannotatedSchemaDefinitionClass::class)
        }
    }

    @Schema(concepts = [])
    private class NonInterfaceSchemaDefinitionClass

    @Test
    fun `test non-interface schema class should throw an exception`() {
        assertThrows(NotInterfaceMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(NonInterfaceSchemaDefinitionClass::class)
        }
    }

    @Schema(concepts = [])
    @Concept(facets = [])
    private interface SchemaDefinitionClassWithConceptAnnotation

    @Test
    fun `test schema class with concept annotation should throw an exception`() {
        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaDefinitionClassWithConceptAnnotation::class)
        }
    }

    @Schema(concepts = [])
    @StringFacet
    private interface SchemaDefinitionClassWithFacetAnnotation

    @Test
    fun `test schema class with facet annotation should throw an exception`() {
        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaDefinitionClassWithFacetAnnotation::class)
        }
    }

    @Schema(concepts = [])
    private interface ParentSchemaWithTwoSchemaAnnotationsInHierarchyClasses
    @Schema(concepts = [])
    private interface SchemaWithTwoSchemaAnnotationsInHierarchyClasses:
        ParentSchemaWithTwoSchemaAnnotationsInHierarchyClasses

    @Test
    @Disabled("Not prevented currently")
    fun `test schema with two schema annotations in hierarchy should throw an exception`() {
        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithTwoSchemaAnnotationsInHierarchyClasses::class)
        }
    }


    @Schema(concepts = [])
    private interface EmptySchemaDefinitionClass

    @Test
    fun `test create an empty schema from an empty schema interface`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(EmptySchemaDefinitionClass::class)
        assertEquals(0, schema.numberOfConcepts())
    }

}