package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.exceptions.MissingAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.NotInterfaceSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongClassStructureSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongTypeSyntaxException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SchemaApiSchemaAnnotationTest {

    @Schema(concepts = [])
    private interface EmptySchema

    @Test
    fun `test create an empty schema from an empty schema interface should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = EmptySchema::class) {
            // do nothing
        }
    }

    @Schema(concepts = [])
    private sealed interface SealedInterfaceInsteadOfInterfaceSchema

    @Test
    fun `test sealed interface instead of interface schema class should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SealedInterfaceInsteadOfInterfaceSchema::class) {
            // do nothing
        }
    }

    private interface ParentInterface
    @Schema(concepts = [])
    private interface SchemaWithParentInterface: ParentInterface

    @Test
    fun `test schema with parent interface without annotations should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithParentInterface::class) {
            // do nothing
        }
    }

    private interface UnannotatedSchema

    @Test
    fun `test unannotated schema class should throw an exception`() {
        assertThrows(MissingAnnotationSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = UnannotatedSchema::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [])
    private abstract class AbstractClassInsteadOfInterfaceSchema

    @Test
    fun `test abstract class instead of interface schema class should throw an exception`() {
        assertThrows(NotInterfaceSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = AbstractClassInsteadOfInterfaceSchema::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [])
    private class ClassInsteadOfInterfaceSchema

    @Test
    fun `test class instead of interface schema class should throw an exception`() {
        assertThrows(NotInterfaceSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = ClassInsteadOfInterfaceSchema::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [])
    private enum class EnumInsteadOfInterfaceSchema

    @Test
    fun `test enum class instead of interface schema class should throw an exception`() {
        assertThrows(NotInterfaceSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = EnumInsteadOfInterfaceSchema::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [])
    private object ObjectInsteadOfInterfaceSchema

    @Test
    fun `test object instead of interface schema class should throw an exception`() {
        assertThrows(NotInterfaceSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = ObjectInsteadOfInterfaceSchema::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [])
    private annotation class AnnotationInsteadOfInterfaceSchema

    @Test
    fun `test annotation instead of interface schema class should throw an exception`() {
        assertThrows(NotInterfaceSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = AnnotationInsteadOfInterfaceSchema::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [])
    @Concept(facets = [])
    private interface SchemaWithConceptAnnotation

    @Test
    fun `test schema class with concept annotation should throw an exception`() {
        assertThrows(WrongAnnotationSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptAnnotation::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [])
    @StringFacet
    private interface SchemaWithFacetAnnotation

    @Test
    fun `test schema class with facet annotation should throw an exception`() {
        assertThrows(WrongAnnotationSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithFacetAnnotation::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [])
    private interface ParentSchemaWithTwoSchemaAnnotationsInHierarchyClasses
    @Schema(concepts = [])
    private interface SchemaWithTwoSchemaAnnotationsInHierarchyClasses:
        ParentSchemaWithTwoSchemaAnnotationsInHierarchyClasses

    @Test
    fun `test schema with two schema annotations in hierarchy should throw an exception`() {
        assertThrows(WrongClassStructureSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithTwoSchemaAnnotationsInHierarchyClasses::class) {
                // do nothing
            }
        }
    }

    @Concept(facets = [])
    private interface ParentSchemaWithConceptAnnotation
    @Schema(concepts = [])
    private interface SchemaWithConceptAndSchemaAnnotationsInHierarchyClasses:
        ParentSchemaWithConceptAnnotation

    @Test
    fun `test schema with concept and schema annotations in hierarchy should throw an exception`() {
        assertThrows(WrongClassStructureSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptAndSchemaAnnotationsInHierarchyClasses::class) {
                // do nothing
            }
        }
    }

    @Suppress("Unused")
    @Schema(concepts = [])
    private interface SchemaWithGenericTypeParameter<T>

    @Test
    fun `test schema class with generic type parameter should throw an exception`() {
        assertThrows(WrongTypeSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithGenericTypeParameter::class) {
                // do nothing
            }
        }
    }


}