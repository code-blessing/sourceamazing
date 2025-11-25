package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.BuilderErrorCode.CLASS_MUST_BE_AN_INTERFACE
import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderApiBuilderAndDataProviderAnnotationTest {

    private interface MyConcepts {

        interface MyConcept {
            val text: String
        }

        val concepts: List<MyConcept>
    }

    @Builder private interface EmptyBuilder

    @Test
    fun `test create an empty builder should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, EmptyBuilder::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderWithEmptyDataProvider {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider class DataProvider
    }

    @Test
    fun `test create an empty data provider should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderWithEmptyDataProvider::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder private sealed interface SealedEmptyBuilder

    @Test
    fun `test sealed interface instead of interface builder class should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, SealedEmptyBuilder::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderWithSealedDataProvider {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider sealed class DataProvider
    }

    @Test
    fun `test create an sealed data provider should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderWithSealedDataProvider::class) {
                    // do nothing
                }
            }
        }
    }

    private interface ParentInterface

    @Builder private interface BuilderWithParentInterface : ParentInterface

    @Test
    fun `test builder with parent interface without annotations should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderWithParentInterface::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderWithInheritanceDataProvider {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        open class ParentDataProvider

        @BuilderDataProvider class DataProvider : ParentDataProvider()
    }

    @Test
    fun `test create an data provider with an inheritance should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderWithInheritanceDataProvider::class) {
                    // do nothing
                }
            }
        }
    }

    interface UnannotatedBuilder

    @Test
    fun `test unannotated builder class should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.MUST_HAVE_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, UnannotatedBuilder::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithUnannotatedDataProvider {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        class DataProvider
    }

    @Test
    fun `test unannotated data provider class should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.MUST_HAVE_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderWithUnannotatedDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder private abstract class AbstractClassInsteadOfInterfaceBuilder

    @Test
    fun `test abstract class instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(BuilderSyntaxException::class, CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        AbstractClassInsteadOfInterfaceBuilder::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithAbstractDataProvider {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider abstract class DataProvider
    }

    @Test
    fun `test abstract data provider class should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderWithAbstractDataProvider::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder private class ClassInsteadOfInterfaceSchema

    @Test
    fun `test class instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(BuilderSyntaxException::class, CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, ClassInsteadOfInterfaceSchema::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithInterfaceDataProvider {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider interface DataProvider
    }

    @Test
    fun `test interface data provider class should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderWithInterfaceDataProvider::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder private enum class EnumInsteadOfInterfaceBuilder

    @Test
    fun `test enum class instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(BuilderSyntaxException::class, CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, EnumInsteadOfInterfaceBuilder::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithEnumDataProvider {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider enum class DataProvider
    }

    @Test
    fun `test enum data provider class should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderWithEnumDataProvider::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder private object ObjectInsteadOfInterfaceBuilder

    @Test
    fun `test object instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(BuilderSyntaxException::class, CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, ObjectInsteadOfInterfaceBuilder::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithObjectDataProvider {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider object DataProvider
    }

    @Test
    fun `test object data provider class should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderWithObjectDataProvider::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder private annotation class AnnotationInsteadOfInterfaceBuilder

    @Test
    fun `test annotation instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(BuilderSyntaxException::class, CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        AnnotationInsteadOfInterfaceBuilder::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithAnnotationDataProvider {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider private annotation class DataProvider
    }

    @Test
    fun `test annotation data provider class should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.CLASS_CANNOT_BE_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderWithAnnotationDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder("anotherConcept")
    private interface BuilderWithExpectedAliasFromSuperiorBuilderAnnotation

    @Test
    fun `test top level builder with ExpectedAliasFromSuperiorBuilder should not throw an exception`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderWithExpectedAliasFromSuperiorBuilderAnnotation::class,
                    mapOf("anotherConcept" to conceptNameAndIdentifier),
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderWithDataProviderWithExpectedAliasFromSuperiorBuilderAnnotation {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider @ExpectedAliasFromSuperiorBuilder("anotherConcept") class DataProvider
    }

    @Test
    fun `test data provider class with ExpectedAliasFromSuperiorBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.CAN_NOT_HAVE_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderWithDataProviderWithExpectedAliasFromSuperiorBuilderAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithNestedBuilderHavingExpectedAliasFromSuperiorBuilderAnnotation {
        @Builder @ExpectedAliasFromSuperiorBuilder("foo") private interface NestedBuilder

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(): NestedBuilder
    }

    @Test
    fun `test nested builder with ExpectedAliasFromSuperiorBuilder should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderWithNestedBuilderHavingExpectedAliasFromSuperiorBuilderAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder private interface ParentBuilderWithTwoBuilderAnnotationsInHierarchyClasses

    @Builder
    private interface BuilderWithTwoBuilderAnnotationsInHierarchyClasses :
        ParentBuilderWithTwoBuilderAnnotationsInHierarchyClasses

    @Test
    fun `test builder with two builder annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderWithTwoBuilderAnnotationsInHierarchyClasses::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithDataProviderWithTwoAnnotationsInHierarchy {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: SubDataProvider)

        @BuilderDataProvider open class DataProvider

        @BuilderDataProvider class SubDataProvider : DataProvider()
    }

    @Test
    fun `test data provider with two BuilderDataProvider annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderWithDataProviderWithTwoAnnotationsInHierarchy::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @BuilderDataProvider private interface ParentClassWithDataProviderAnnotation

    @Builder
    private interface BuilderWithSchemaAnnotationsInHierarchyClasses :
        ParentClassWithDataProviderAnnotation

    @Test
    fun `test builder with schema annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.CAN_NOT_HAVE_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderWithSchemaAnnotationsInHierarchyClasses::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithDataProviderWithTwoDifferentAnnotationsInHierarchy {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: SubDataProvider)

        @Builder private open class DataProvider

        @BuilderDataProvider class SubDataProvider : DataProvider()
    }

    @Test
    fun `test data provider with two different annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.CAN_NOT_HAVE_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderWithDataProviderWithTwoDifferentAnnotationsInHierarchy::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder private interface BuilderWithGenericTypeParameter<T>

    @Test
    fun `test builder class with generic type parameter should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.NO_GENERIC_TYPE_PARAMETER,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, BuilderWithGenericTypeParameter::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithDataProviderWithGenericTypeParameter {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider<String>)

        @BuilderDataProvider class DataProvider<T>
    }

    @Test
    fun `test data provider with with generic type parameter should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderWithDataProviderWithGenericTypeParameter::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderWithProperties {
        val builderMethodProperty: String
    }

    @Test
    fun `test builder class with properties should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.CLASS_CANNOT_HAVE_PROPERTIES,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, BuilderWithProperties::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithDataProviderWithPropertiesAndMethods {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider<String>)

        @BuilderDataProvider
        class DataProvider<T> {
            val dataProviderProperty: String = "hallo"

            fun calculateSum(a: Int, b: Int): Int = a + b

            @BuilderData
            @NewConcept(concept = MyConcepts.MyConcept::class, declareConceptAlias = "foo")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun doSomething() {
                // nothing to do
            }
        }
    }

    @Test
    fun `test data provider with properties and methods should should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderWithDataProviderWithPropertiesAndMethods::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithExtensionType {
        interface MyExtensionType

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, "foo")
        @SetRandomConceptIdentifierValue("foo")
        fun MyExtensionType.doSomething()
    }

    @Test
    fun `test builder method with extension type should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, BuilderMethodWithExtensionType::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithDataProviderWithExtensionType {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider<String>)

        @BuilderDataProvider
        class DataProvider<T> {
            interface MyExtensionType {
                fun getText() = "hello"
            }

            @BuilderData
            @NewConcept(concept = MyConcepts.MyConcept::class, declareConceptAlias = "foo")
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
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_DATA_PROVIDER_FUNCTION_CAN_NOT_BE_EXTENSION_FUNCTION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderWithDataProviderWithExtensionType::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithDataProviderWithMethodsWithParameters {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @NewConcept(concept = MyConcepts.MyConcept::class, declareConceptAlias = "foo")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun doSomething(myParameter: String) {
                // nothing to do
            }
        }
    }

    @Test
    fun `test data provider with methods having parameter should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_DATA_PROVIDER_FUNCTION_HAS_PARAMETERS,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderWithDataProviderWithMethodsWithParameters::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderWithDataProviderWithMethodReturningNothing {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @NewConcept(concept = MyConcepts.MyConcept::class, declareConceptAlias = "foo")
            @SetProvidedFacetValue(
                conceptToModifyAlias = "foo",
                facetToModify = "MyConcept",
            )
            fun getText() {
                // nothing to return
            }
        }
    }

    @Test
    fun `test data provider with method returning nothing should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_DATA_PROVIDER_FUNCTION_RETURNS_NOTHING,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderWithDataProviderWithMethodReturningNothing::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }
}
