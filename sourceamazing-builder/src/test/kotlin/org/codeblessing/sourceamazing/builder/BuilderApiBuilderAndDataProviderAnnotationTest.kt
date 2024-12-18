package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderDataProvider
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.ProvideBuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.exceptions.CanNotBeAnnotationTypeSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.MissingClassAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.NotInterfaceSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongClassStructureSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongTypeSyntaxException
import org.junit.jupiter.api.Test

class BuilderApiBuilderAndDataProviderAnnotationTest {

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
    private interface BuilderWithEmptyDataProvider {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        class DataProvider
    }

    @Test
    fun `test create an empty data provider should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderWithEmptyDataProvider::class) {
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

    @Builder
    private interface BuilderWithSealedDataProvider {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        sealed class DataProvider
    }

    @Test
    fun `test create an sealed data provider should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderWithSealedDataProvider::class) {
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

    @Builder
    private interface BuilderWithInheritanceDataProvider {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        open class ParentDataProvider


        @BuilderDataProvider
        class DataProvider: ParentDataProvider()
    }

    @Test
    fun `test create an data provider with an inheritance should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderWithInheritanceDataProvider::class) {
                // do nothing
            }
        }
    }

    interface UnannotatedBuilder

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
    private interface BuilderWithUnannotatedDataProvider {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        class DataProvider
    }

    @Test
    fun `test unannotated data provider class should throw an exception`() {
        assertExceptionWithErrorCode(MissingClassAnnotationSyntaxException::class, SchemaErrorCode.MUST_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithUnannotatedDataProvider::class) {
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
    private interface BuilderWithAbstractDataProvider {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        abstract class DataProvider
    }

    @Test
    fun `test abstract data provider class should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderWithAbstractDataProvider::class) {
                // do nothing
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
    private interface BuilderWithInterfaceDataProvider {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        interface DataProvider
    }

    @Test
    fun `test interface data provider class should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderWithInterfaceDataProvider::class) {
                // do nothing
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
    private interface BuilderWithEnumDataProvider {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        enum class DataProvider
    }

    @Test
    fun `test enum data provider class should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderWithEnumDataProvider::class) {
                // do nothing
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
    private interface BuilderWithObjectDataProvider {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        object DataProvider
    }

    @Test
    fun `test object data provider class should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderWithObjectDataProvider::class) {
                // do nothing
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
    private interface BuilderWithAnnotationDataProvider {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        private annotation class DataProvider
    }

    @Test
    fun `test annotation data provider class should throw an exception`() {
        assertExceptionWithErrorCode(CanNotBeAnnotationTypeSyntaxException::class, SchemaErrorCode.CLASS_CANNOT_BE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithAnnotationDataProvider::class) {
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
    private interface BuilderWithDataProviderWithSchemaAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        @Schema(concepts = [])
        class DataProvider
    }

    @Test
    fun `test data provider class with schema annotation should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithDataProviderWithSchemaAnnotation::class) {
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
    private interface BuilderWithDataProviderWithConceptAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        @Concept(facets = [])
        class DataProvider
    }

    @Test
    fun `test data provider class with concept annotation should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithDataProviderWithConceptAnnotation::class) {
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
    private interface BuilderWithDataProviderWithStringFacetAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        @StringFacet
        class DataProvider
    }

    @Test
    fun `test data provider class with StringFacet annotation should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithDataProviderWithStringFacetAnnotation::class) {
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
    private interface BuilderWithDataProviderWithExpectedAliasFromSuperiorBuilderAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        @ExpectedAliasFromSuperiorBuilder()
        class DataProvider
    }

    @Test
    fun `test data provider class with ExpectedAliasFromSuperiorBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithDataProviderWithExpectedAliasFromSuperiorBuilderAnnotation::class) {
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

    @Builder
    private interface BuilderWithDataProviderWithTwoAnnotationsInHierarchy {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: SubDataProvider
        )

        @BuilderDataProvider
        open class DataProvider

        @BuilderDataProvider
        class SubDataProvider: DataProvider()

    }

    @Test
    fun `test data provider with two BuilderDataProvider annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithDataProviderWithTwoAnnotationsInHierarchy::class) {
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

    @Builder
    private interface BuilderWithDataProviderWithTwoDifferentAnnotationsInHierarchy {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: SubDataProvider
        )

        @Schema(concepts = [])
        private open class DataProvider

        @BuilderDataProvider
        class SubDataProvider: DataProvider()

    }

    @Test
    fun `test data provider with two different annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithDataProviderWithTwoDifferentAnnotationsInHierarchy::class) {
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

    @Builder
    private interface BuilderWithDataProviderWithGenericTypeParameter {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider<String>
        )

        @Suppress("UNUSED")
        @BuilderDataProvider
        class DataProvider<T>
    }

    @Test
    fun `test data provider with with generic type parameter should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderWithDataProviderWithGenericTypeParameter::class) {
                // do nothing
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

    @Builder
    private interface BuilderWithDataProviderWithPropertiesAndMethods {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider<String>
        )

        @Suppress("UNUSED")
        @BuilderDataProvider
        class DataProvider<T> {
            val dataProviderProperty: String = "hallo"

            fun calculateSum(a: Int, b: Int): Int = a + b

            @BuilderData
            @NewConcept(concept = SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class, declareConceptAlias = "foo")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun doSomething() {
                // nothing to do
            }
        }
    }

    @Test
    fun `test data provider with properties and methods should should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderWithDataProviderWithPropertiesAndMethods::class) {
                // do nothing
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

    @Builder
    private interface BuilderWithDataProviderWithExtensionType {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider<String>
        )

        @Suppress("UNUSED")
        @BuilderDataProvider
        class DataProvider<T> {
            interface MyExtensionType {
                fun getText() = "hello"
            }

            @BuilderData
            @NewConcept(concept = SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class, declareConceptAlias = "foo")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun MyExtensionType.doSomething() {
                this.getText()
            }

            @BuilderData
            fun doNothing() {
                // do nothing
            }


        }
    }

    @Test
    fun `test data provider with extension type methods should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_DATA_PROVIDER_FUNCTION_CAN_NOT_BE_EXTENSION_FUNCTION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithDataProviderWithExtensionType::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderWithDataProviderWithMethodsWithParameters {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @Suppress("UNUSED")
        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED_PARAMETER")
            @BuilderData
            @NewConcept(concept = SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class, declareConceptAlias = "foo")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun doSomething(myParameter: String) {
                // nothing to do
            }
        }
    }

    @Test
    fun `test data provider with methods having parameter should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_DATA_PROVIDER_FUNCTION_HAS_PARAMETERS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithDataProviderWithMethodsWithParameters::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderWithDataProviderWithMethodReturningNothing {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @Suppress("UNUSED")
        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @NewConcept(concept = SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class, declareConceptAlias = "foo")
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
            fun getText() {
                // nothing to return
            }
        }
    }

    @Test
    fun `test data provider with method returning nothing should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_DATA_PROVIDER_FUNCTION_RETURNS_NOTHING) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithDataProviderWithMethodReturningNothing::class) {
                    // do nothing
                }
            }
        }
    }

}