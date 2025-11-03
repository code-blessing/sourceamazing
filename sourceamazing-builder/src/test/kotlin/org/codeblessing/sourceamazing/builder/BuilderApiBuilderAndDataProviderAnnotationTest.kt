package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.exceptions.*
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.codeblessing.sourceamazing.toConceptName
import org.junit.jupiter.api.Test

class BuilderApiBuilderAndDataProviderAnnotationTest {

    private interface SchemaWithConceptWithTextFacet {

        interface ConceptWithTextFacet {
            @Suppress("UNUSED")
            val text: String
        }

        @Suppress("UNUSED")
        val concepts: List<ConceptWithTextFacet>

    }

    @Builder
    @ExpectedRootAlias("root")
    private interface EmptyBuilder

    @Test
    fun `test create an empty builder should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    EmptyBuilder::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
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
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderWithEmptyDataProvider::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private sealed interface SealedEmptyBuilder

    @Test
    fun `test sealed interface instead of interface builder class should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    SealedEmptyBuilder::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
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
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderWithSealedDataProvider::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    private interface ParentInterface

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderWithParentInterface : ParentInterface

    @Test
    fun `test builder with parent interface without annotations should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderWithParentInterface::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderWithInheritanceDataProvider {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        open class ParentDataProvider


        @BuilderDataProvider
        class DataProvider : ParentDataProvider()
    }

    @Test
    fun `test create an data provider with an inheritance should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderWithInheritanceDataProvider::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    interface UnannotatedBuilder

    @Test
    fun `test unannotated builder class should throw an exception`() {
        assertExceptionWithErrorCode(
            MissingClassAnnotationSyntaxException::class,
            SchemaErrorCode.MUST_HAVE_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        UnannotatedBuilder::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
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
        assertExceptionWithErrorCode(
            MissingClassAnnotationSyntaxException::class,
            SchemaErrorCode.MUST_HAVE_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithUnannotatedDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private abstract class AbstractClassInsteadOfInterfaceBuilder

    @Test
    fun `test abstract class instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        AbstractClassInsteadOfInterfaceBuilder::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
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
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderWithAbstractDataProvider::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private class ClassInsteadOfInterfaceSchema

    @Test
    fun `test class instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        ClassInsteadOfInterfaceSchema::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
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
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderWithInterfaceDataProvider::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private enum class EnumInsteadOfInterfaceBuilder

    @Test
    fun `test enum class instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        EnumInsteadOfInterfaceBuilder::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
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
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderWithEnumDataProvider::class,
                ) {
                    // do nothing
                }
            }
        }
    }


    @Builder
    @ExpectedRootAlias("root")
    private object ObjectInsteadOfInterfaceBuilder

    @Test
    fun `test object instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        ObjectInsteadOfInterfaceBuilder::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
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
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderWithObjectDataProvider::class,
                ) {
                    // do nothing
                }
            }
        }
    }


    @Builder
    @ExpectedRootAlias("root")
    private annotation class AnnotationInsteadOfInterfaceBuilder

    @Test
    fun `test annotation instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        AnnotationInsteadOfInterfaceBuilder::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
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
        assertExceptionWithErrorCode(
            CanNotBeAnnotationTypeSyntaxException::class,
            SchemaErrorCode.CLASS_CANNOT_BE_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithAnnotationDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    @ExpectedAliasFromSuperiorBuilder("anotherConcept")
    private interface BuilderWithExpectedAliasFromSuperiorBuilderAnnotation

    @Test
    fun `test top level builder with ExpectedAliasFromSuperiorBuilder should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithExpectedAliasFromSuperiorBuilderAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderWithDataProviderWithExpectedAliasFromSuperiorBuilderAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        @ExpectedAliasFromSuperiorBuilder("anotherConcept")
        class DataProvider
    }

    @Test
    fun `test data provider class with ExpectedAliasFromSuperiorBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithDataProviderWithExpectedAliasFromSuperiorBuilderAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderWithNestedBuilderHavingExpectedAliasFromSuperiorBuilderAnnotation {
        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(): NestedBuilder
    }

    @Test
    fun `test nested builder with ExpectedAliasFromSuperiorBuilder should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderWithNestedBuilderHavingExpectedAliasFromSuperiorBuilderAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface ParentBuilderWithTwoBuilderAnnotationsInHierarchyClasses

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderWithTwoBuilderAnnotationsInHierarchyClasses :
        ParentBuilderWithTwoBuilderAnnotationsInHierarchyClasses

    @Test
    fun `test builder with two builder annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongAnnotationSyntaxException::class,
            SchemaErrorCode.NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithTwoBuilderAnnotationsInHierarchyClasses::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderWithDataProviderWithTwoAnnotationsInHierarchy {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: SubDataProvider
        )

        @BuilderDataProvider
        open class DataProvider

        @BuilderDataProvider
        class SubDataProvider : DataProvider()

    }

    @Test
    fun `test data provider with two BuilderDataProvider annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongAnnotationSyntaxException::class,
            SchemaErrorCode.NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithDataProviderWithTwoAnnotationsInHierarchy::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @BuilderDataProvider
    private interface ParentClassWithDataProviderAnnotation

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderWithSchemaAnnotationsInHierarchyClasses :
        ParentClassWithDataProviderAnnotation

    @Test
    fun `test builder with schema annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithSchemaAnnotationsInHierarchyClasses::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderWithDataProviderWithTwoDifferentAnnotationsInHierarchy {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: SubDataProvider
        )

        @Builder
        private open class DataProvider

        @BuilderDataProvider
        class SubDataProvider : DataProvider()

    }

    @Test
    fun `test data provider with two different annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithDataProviderWithTwoDifferentAnnotationsInHierarchy::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }


    @Suppress("Unused")
    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderWithGenericTypeParameter<T>

    @Test
    fun `test builder class with generic type parameter should throw an exception`() {
        assertExceptionWithErrorCode(WrongTypeSyntaxException::class, SchemaErrorCode.NO_GENERIC_TYPE_PARAMETER) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithGenericTypeParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
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
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderWithDataProviderWithGenericTypeParameter::class,
                ) {
                    // do nothing
                }
            }
        }
    }


    @Suppress("Unused")
    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderWithProperties {
        val builderMethodProperty: String
    }

    @Test
    fun `test builder class with properties should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongClassStructureSyntaxException::class,
            SchemaErrorCode.CLASS_CANNOT_HAVE_PROPERTIES,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithProperties::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
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
            @NewConcept(
                concept = SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
                declareConceptAlias = "foo",
            )
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun doSomething() {
                // nothing to do
            }
        }
    }

    @Test
    fun `test data provider with properties and methods should should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderWithDataProviderWithPropertiesAndMethods::class,
                ) {
                    // do nothing
                }
            }
        }
    }


    @Suppress("Unused")
    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithExtensionType {
        interface MyExtensionType

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class, "foo")
        @SetRandomConceptIdentifierValue("foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun MyExtensionType.doSomething()
    }

    @Test
    fun `test builder method with extension type should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongClassStructureSyntaxException::class,
            SchemaErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithExtensionType::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
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
            @NewConcept(
                concept = SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
                declareConceptAlias = "foo",
            )
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
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
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithDataProviderWithExtensionType::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
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
            @NewConcept(
                concept = SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
                declareConceptAlias = "foo",
            )
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
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
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithDataProviderWithMethodsWithParameters::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
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
            @NewConcept(
                concept = SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
                declareConceptAlias = "foo",
            )
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "ConceptWithTextFacet")
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
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
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithDataProviderWithMethodReturningNothing::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

}
