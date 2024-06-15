package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.exceptions.MissingClassAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.NotInterfaceSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongClassStructureSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongTypeSyntaxException
import org.junit.jupiter.api.Test

class BuilderApiBuilderAnnotationTest {

    @Schema(concepts = [SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class])
    private interface SchemaWithConceptWithTextFacet {
        @Concept(facets = [ConceptWithTextFacet.TextFacet::class])
        interface ConceptWithTextFacet {
            @StringFacet
            interface TextFacet
        }
    }

    @Builder
    private interface EmptyBuilder

    @Test
    fun `test create an empty builder should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, EmptyBuilder::class) { 
                // do nothing
            }
        }
    }

    @Builder
    private sealed interface SealedEmptyBuilder

    @Test
    fun `test sealed interface instead of interface builder class should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, SealedEmptyBuilder::class) { 
                // do nothing
            }
        }
    }

    private interface ParentInterface
    @Builder
    private interface BuilderWithParentInterface: ParentInterface

    @Test
    fun `test builder with parent interface without annotations should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderWithParentInterface::class) { 
                // do nothing
            }
        }
    }

    private interface UnannotatedBuilder

    @Test
    fun `test unannotated builder class should throw an exception`() {
        assertExceptionWithErrorCode(MissingClassAnnotationSyntaxException::class, SchemaErrorCode.MUST_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, UnannotatedBuilder::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private abstract class AbstractClassInsteadOfInterfaceBuilder

    @Test
    fun `test abstract class instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, AbstractClassInsteadOfInterfaceBuilder::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private class ClassInsteadOfInterfaceSchema

    @Test
    fun `test class instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, ClassInsteadOfInterfaceSchema::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private enum class EnumInsteadOfInterfaceBuilder

    @Test
    fun `test enum class instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, EnumInsteadOfInterfaceBuilder::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private object ObjectInsteadOfInterfaceBuilder

    @Test
    fun `test object instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, ObjectInsteadOfInterfaceBuilder::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private annotation class AnnotationInsteadOfInterfaceBuilder

    @Test
    fun `test annotation instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, AnnotationInsteadOfInterfaceBuilder::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    @Schema(concepts = [])
    private interface BuilderWithSchemaAnnotation

    @Test
    fun `test builder class with schema annotation should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithSchemaAnnotation::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    @Concept(facets = [])
    private interface BuilderWithConceptAnnotation

    @Test
    fun `test builder class with concept annotation should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithConceptAnnotation::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    @StringFacet
    private interface BuilderWithFacetAnnotation

    @Test
    fun `test builder class with facet annotation should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithFacetAnnotation::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder()
    private interface BuilderWithExpectedAliasFromSuperiorBuilderAnnotation

    @Test
    fun `test top level builder with ExpectedAliasFromSuperiorBuilder should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithExpectedAliasFromSuperiorBuilderAnnotation::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderWithNestedBuilderHavingExpectedAliasFromSuperiorBuilderAnnotation {
        @Builder
        @ExpectedAliasFromSuperiorBuilder()
        private interface NestedBuilder

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(): NestedBuilder
    }

    @Test
    fun `test nested builder with ExpectedAliasFromSuperiorBuilder should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderWithNestedBuilderHavingExpectedAliasFromSuperiorBuilderAnnotation::class) { 
                // do nothing
            }
        }
    }

    @Builder
    private interface ParentBuilderWithTwoBuilderAnnotationsInHierarchyClasses
    @Builder
    private interface BuilderWithTwoBuilderAnnotationsInHierarchyClasses:
        ParentBuilderWithTwoBuilderAnnotationsInHierarchyClasses

    @Test
    fun `test builder with two builder annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithTwoBuilderAnnotationsInHierarchyClasses::class) { 
                    // do nothing
                }
            }
        }
    }

    @Schema(concepts = [])
    private interface ParentClassWithSchemaAnnotation
    @Builder
    private interface BuilderWithSchemaAnnotationsInHierarchyClasses:
        ParentClassWithSchemaAnnotation

    @Test
    fun `test builder with schema annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithSchemaAnnotationsInHierarchyClasses::class) { 
                    // do nothing
                }
            }
        }
    }

    @Suppress("Unused")
    @Builder
    private interface BuilderWithGenericTypeParameter<T>

    @Test
    fun `test builder class with generic type parameter should throw an exception`() {
        assertExceptionWithErrorCode(WrongTypeSyntaxException::class, SchemaErrorCode.NO_GENERIC_TYPE_PARAMETER) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithGenericTypeParameter::class) { 
                    // do nothing
                }
            }
        }
    }

    @Suppress("Unused")
    @Builder
    private interface BuilderWithProperties {
        val builderMethodProperty: String
    }

    @Test
    fun `test builder class with properties should throw an exception`() {
        assertExceptionWithErrorCode(WrongClassStructureSyntaxException::class, SchemaErrorCode.CLASS_CANNOT_HAVE_PROPERTIES) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithProperties::class) {
                    // do nothing
                }
            }
        }
    }

    @Suppress("Unused")
    @Builder
    private interface BuilderMethodWithExtensionType {
        interface MyExtensionType

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun MyExtensionType.doSomething()
    }

    @Test
    fun `test builder method with extension type should throw an exception`() {
        assertExceptionWithErrorCode(WrongClassStructureSyntaxException::class, SchemaErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithExtensionType::class) {
                    // do nothing
                }
            }
        }
    }

}