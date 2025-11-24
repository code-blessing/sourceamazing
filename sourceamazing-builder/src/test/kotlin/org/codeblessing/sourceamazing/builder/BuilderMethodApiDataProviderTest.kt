package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.DataProviderInvocationRuntimeException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Suppress("UNUSED")
class BuilderMethodApiDataProviderTest {

    private interface SchemaWithConceptWithTextFacet {
        interface ConceptWithTextFacet {
            val text: String
        }

        val concepts: List<ConceptWithTextFacet>
    }

    @BuilderDataProvider class EmptyBuilderDataProvider

    @Builder
    private interface BuilderMethodWithDataProviderAsLambdaFunction {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
            declareConceptAlias = "foo",
        )
        fun doSomething(
            @SetConceptIdentifierValue("foo") conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: EmptyBuilderDataProvider.() -> Unit,
        )
    }

    @Test
    fun `test builder data provider passing an extension function as parameter should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_DATA_PROVIDER_PARAMS_INVALID,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithDataProviderAsLambdaFunction::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderDataProviderWithLambdaFunction {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
            declareConceptAlias = "foo",
        )
        fun doSomething(
            @SetConceptIdentifierValue("foo") conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: () -> Unit,
        )
    }

    @Test
    fun `test builder data provider passing a lambda function as parameter should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_DATA_PROVIDER_PARAMS_INVALID,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithBuilderDataProviderWithLambdaFunction::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderDataProviderWithArray {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
            declareConceptAlias = "foo",
        )
        fun doSomething(
            @SetConceptIdentifierValue("foo") conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: Array<EmptyBuilderDataProvider>,
        )
    }

    @Test
    fun `test builder data provider passing an array as parameter should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.MUST_HAVE_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithBuilderDataProviderWithArray::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderDataProviderWithAnyObject {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
            declareConceptAlias = "foo",
        )
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo")
            conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: Any,
        )
    }

    @Test
    fun `test builder data provider passing an unannotated object should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.MUST_HAVE_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithBuilderDataProviderWithAnyObject::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithBuilderDataAndIgnoreNullFacetValue {
        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
            declareConceptAlias = "foo",
        )
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo")
            conceptIdentifier: ConceptIdentifier,
            @IgnoreNullFacetValue @ProvideBuilderData data: EmptyBuilderDataProvider,
        )
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and ProvideBuilderData annotation on same method should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_DATA_PROVIDER_AND_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodParamWithBuilderDataAndIgnoreNullFacetValue::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithBuilderDataAsNullableParam {
        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
            declareConceptAlias = "foo",
        )
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo")
            conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: EmptyBuilderDataProvider?,
        )
    }

    @Test
    fun `test builder data provider passing a data object with nullable parameter type should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_DATA_PROVIDER_CANNOT_BE_NULLABLE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodParamWithBuilderDataAsNullableParam::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithEmptyDataProvider {
        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
            declareConceptAlias = "foo",
        )
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo")
            conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: EmptyBuilderDataProvider,
        )
    }

    @Test
    fun `test builder data provider passing an annotated data provider object should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodParamWithEmptyDataProvider::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithBuilderDataWithOtherAnnotations {
        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
            declareConceptAlias = "foo",
        )
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo")
            conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: EmptyBuilderDataProviderWithOtherAnnotations,
        )

        @BuilderDataProvider @Builder class EmptyBuilderDataProviderWithOtherAnnotations
    }

    @Test
    fun `test builder data provider passing a data object with other annotations from source amazing should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.CAN_NOT_HAVE_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodParamWithBuilderDataWithOtherAnnotations::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithBuilderDataWithOtherNonSourceamazingAnnotation {
        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
            declareConceptAlias = "foo",
        )
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo")
            conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: EmptyBuilderDataProviderWithOtherNonSourceamazingAnnotation,
        )

        @BuilderDataProvider
        @FunctionalInterface
        class EmptyBuilderDataProviderWithOtherNonSourceamazingAnnotation
    }

    @Test
    fun `test builder data provider passing a data object with other annotations not from source amazing should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodParamWithBuilderDataWithOtherNonSourceamazingAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithGenericParameter {
        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
            declareConceptAlias = "foo",
        )
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo")
            conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: BuilderDataProviderWithGenericParameter<String>,
        )

        @BuilderDataProvider
        class BuilderDataProviderWithGenericParameter<T>(private val data: T) {

            fun getGenericValue(): T {
                return data
            }

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "text")
            fun getText(): String {
                return "hallo"
            }
        }
    }

    @Test
    fun `test builder data provider passing a data object with generic parameter but not returning it should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodDataProviderWithGenericParameter::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderUsingAndReturningGenericParameter {
        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
            declareConceptAlias = "foo",
        )
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo")
            conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: BuilderDataProviderWithGenericParameter<String>,
        )

        @BuilderDataProvider
        class BuilderDataProviderWithGenericParameter<T>(private val data: T) {
            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "text")
            fun getGenericValue(): T {
                return data
            }

            fun getText(): String {
                return "hallo"
            }
        }
    }

    @Test
    fun `test builder data provider passing a data object with generic parameter and returning the generic object should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_FACET_VALUE_PARAMETER,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderUsingAndReturningGenericParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderThrowingException {
        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doThrowAnException(@ProvideBuilderData data: BuilderDataProviderThrowingException)

        @BuilderDataProvider
        class BuilderDataProviderThrowingException {
            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "text")
            fun getText(): String {
                throw NoSuchElementException("This facet value is not available.")
            }
        }
    }

    @Test
    fun `test builder data provider method throwing an exception should fail with this exception`() {
        val builderDataProvider =
            BuilderMethodDataProviderThrowingException.BuilderDataProviderThrowingException()
        val exception =
            assertThrows<DataProviderInvocationRuntimeException> {
                SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                    withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) {
                        BuilderApi.withBuilder(
                            schemaContext,
                            BuilderMethodDataProviderThrowingException::class,
                        ) { builder ->
                            builder.doThrowAnException(builderDataProvider)
                        }
                    }
                }
            }

        assertEquals("This facet value is not available.", exception.cause?.message)
    }
}
