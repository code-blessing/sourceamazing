package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.exceptions.MissingClassAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.NotInterfaceSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongTypeSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.DuplicateConceptSchemaSyntaxException
import org.junit.jupiter.api.Test

class SchemaApiConceptAnnotationTest {

    @Schema(concepts = [SchemaWithEmptyConcept.EmptyConcept::class])
    private interface SchemaWithEmptyConcept {

        @Concept(facets = [])
        interface EmptyConcept
    }

    @Test
    fun `test create an schema with an empty concept class does not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithEmptyConcept::class) {
            // do nothing
        }
    }

    @Schema(concepts = [SchemaWithUnannotatedConcept.UnannotatedConcept::class])
    private interface SchemaWithUnannotatedConcept {
        interface UnannotatedConcept
    }

    @Test
    fun `test unannotated concept class should throw an exception`() {
        assertExceptionWithErrorCode(MissingClassAnnotationSyntaxException::class, SchemaErrorCode.MUST_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithUnannotatedConcept::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaWithConceptClassInsteadOfInterface.ConceptClassInsteadOfInterface::class])
    private interface SchemaWithConceptClassInsteadOfInterface {

        @Concept(facets = [])
        class ConceptClassInsteadOfInterface
    }

    @Test
    fun `test concept class instead of interface should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptClassInsteadOfInterface::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaWithConceptEnumInsteadOfInterface.ConceptEnumInsteadOfInterface::class])
    private interface SchemaWithConceptEnumInsteadOfInterface {

        @Concept(facets = [])
        enum class ConceptEnumInsteadOfInterface
    }

    @Test
    fun `test concept enum instead of interface should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptEnumInsteadOfInterface::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaWithConceptObjectInsteadOfInterface.ConceptObjectInsteadOfInterface::class])
    private interface SchemaWithConceptObjectInsteadOfInterface {

        @Concept(facets = [])
        object ConceptObjectInsteadOfInterface
    }

    @Test
    fun `test concept object instead of interface should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptObjectInsteadOfInterface::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaWithConceptAnnotationInterfaceInsteadOfInterface.ConceptAnnotationInterfaceInsteadOfInterface::class])
    private interface SchemaWithConceptAnnotationInterfaceInsteadOfInterface {

        @Concept(facets = [])
        annotation class ConceptAnnotationInterfaceInsteadOfInterface
    }

    @Test
    fun `test concept annotation interface instead of interface should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptAnnotationInterfaceInsteadOfInterface::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaWithConceptHavingSchemaAnnotation.ConceptWithSchemaAnnotation::class])
    private interface SchemaWithConceptHavingSchemaAnnotation {

        @Concept(facets = [])
        @Schema(concepts = [])
        interface ConceptWithSchemaAnnotation
    }

    @Test
    fun `test create concept class with a schema annotation should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptHavingSchemaAnnotation::class) {
                // do nothing
            }

        }
    }

    @Schema(concepts = [SchemaWithConceptHavingFacetAnnotation.ConceptWithFacetAnnotation::class])
    private interface SchemaWithConceptHavingFacetAnnotation {

        @Concept(facets = [])
        @StringFacet
        interface ConceptWithFacetAnnotation
    }

    @Test
    fun `test concept class with a facet annotation should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptHavingFacetAnnotation::class) {
                // do nothing
            }

        }
    }

    @Schema(concepts = [
        SchemaWithDuplicateConcepts.DuplicateConcept::class,
        SchemaWithDuplicateConcepts.DuplicateConcept::class,
    ])
    private interface SchemaWithDuplicateConcepts {

        @Concept(facets = [])
        interface DuplicateConcept
    }

    @Test
    fun `test duplicate concept classes in schema annotation should throw an exception`() {
        assertExceptionWithErrorCode(DuplicateConceptSchemaSyntaxException::class, SchemaErrorCode.DUPLICATE_CONCEPTS_ON_SCHEMA) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithDuplicateConcepts::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [
        SchemaWithTwoConceptAnnotationsInHierarchy.ChildConcept::class,
    ])
    private interface SchemaWithTwoConceptAnnotationsInHierarchy {

        @Concept(facets = [])
        interface ParentConcept
        @Concept(facets = [])
        interface ChildConcept: ParentConcept
    }

    @Test
    fun `test concept with two concept annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithTwoConceptAnnotationsInHierarchy::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [
        SchemaWithConceptWithTypeParameter.ConceptWithTypeParameter::class,
    ])
    private interface SchemaWithConceptWithTypeParameter {

        @Suppress("Unused")
        @Concept(facets = [])
        interface ConceptWithTypeParameter<T>
    }

    @Test
    fun `test concept with generic type parameter should throw an exception`() {
        assertExceptionWithErrorCode(WrongTypeSyntaxException::class, SchemaErrorCode.NO_GENERIC_TYPE_PARAMETER) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTypeParameter::class) {
                // do nothing
            }
        }
    }

}