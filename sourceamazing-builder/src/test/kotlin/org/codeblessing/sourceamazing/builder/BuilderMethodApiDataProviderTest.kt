package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderDataProvider
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.ProvideBuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.DataProviderInvocationRuntimeException
import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.exceptions.MissingClassAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongAnnotationSyntaxException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderMethodApiDataProviderTest {

    @Schema(concepts = [SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class])
    private interface SchemaWithConceptWithTextFacet {
        @Concept(facets = [ConceptWithTextFacet.TextFacet::class])
        interface ConceptWithTextFacet {
            @StringFacet
            interface TextFacet
        }
    }

    @BuilderDataProvider
    class EmptyBuilderDataProvider


    @Builder
    private interface BuilderMethodWithDataProviderAsLambdaFunction {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: EmptyBuilderDataProvider.() -> Unit,
        )
    }

    @Test
    fun `test builder data provider passing an extension function as parameter should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_DATA_PROVIDER_PARAMS_INVALID) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithDataProviderAsLambdaFunction::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderDataProviderWithLambdaFunction {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: () -> Unit,
        )
    }

    @Test
    fun `test builder data provider passing a lambda function as parameter should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_DATA_PROVIDER_PARAMS_INVALID) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderDataProviderWithLambdaFunction::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderDataProviderWithArray {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: Array<EmptyBuilderDataProvider>,
        )
    }

    @Test
    fun `test builder data provider passing an array as parameter should throw an exception`() {
        assertExceptionWithErrorCode(MissingClassAnnotationSyntaxException::class, SchemaErrorCode.MUST_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderDataProviderWithArray::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderDataProviderWithAnyObject {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: Any,
        )
    }

    @Test
    fun `test builder data provider passing an unannotated object should throw an exception`() {
        assertExceptionWithErrorCode(MissingClassAnnotationSyntaxException::class, SchemaErrorCode.MUST_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderDataProviderWithAnyObject::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithBuilderDataAndIgnoreNullFacetValue {
        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @IgnoreNullFacetValue @ProvideBuilderData data: EmptyBuilderDataProvider,
        )
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and ProvideBuilderData annotation on same method should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_DATA_PROVIDER_AND_IGNORE_NULL_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodParamWithBuilderDataAndIgnoreNullFacetValue::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithBuilderDataAsNullableParam {
        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: EmptyBuilderDataProvider?,
        )
    }

    @Test
    fun `test builder data provider passing a data object with nullable parameter type should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_DATA_PROVIDER_CANNOT_BE_NULLABLE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodParamWithBuilderDataAsNullableParam::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithEmptyDataProvider {
        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: EmptyBuilderDataProvider,
        )
    }

    @Test
    fun `test builder data provider passing an annotated data provider object should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodParamWithEmptyDataProvider::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithBuilderDataWithOtherAnnotations {
        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: EmptyBuilderDataProviderWithOtherAnnotations,
        )

        @BuilderDataProvider
        @Builder
        class EmptyBuilderDataProviderWithOtherAnnotations
    }

    @Test
    fun `test builder data provider passing a data object with other annotations from source amazing should throw an exception`() {
        assertExceptionWithErrorCode(WrongAnnotationSyntaxException::class, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodParamWithBuilderDataWithOtherAnnotations::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithBuilderDataWithOtherNonSourceamazingAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: EmptyBuilderDataProviderWithOtherNonSourceamazingAnnotation,
        )

        @BuilderDataProvider
        @FunctionalInterface
        class EmptyBuilderDataProviderWithOtherNonSourceamazingAnnotation

    }

    @Test
    fun `test builder data provider passing a data object with other annotations not from source amazing should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodParamWithBuilderDataWithOtherNonSourceamazingAnnotation::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithGenericParameter {
        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: BuilderDataProviderWithGenericParameter<String>,
        )

        @Suppress("UNUSED")
        @BuilderDataProvider
        class BuilderDataProviderWithGenericParameter<T>(
            private val data: T,
        ) {

            fun getGenericValue(): T {
                return data
            }

            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.TextFacet::class)
            fun getText(): String {
                return "hallo"
            }
        }
    }

    @Test
    fun `test builder data provider passing a data object with generic parameter but not returning it should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithGenericParameter::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderUsingAndReturningGenericParameter {
        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @ProvideBuilderData data: BuilderDataProviderWithGenericParameter<String>,
        )

        @Suppress("UNUSED")
        @BuilderDataProvider
        class BuilderDataProviderWithGenericParameter<T>(
            private val data: T,
        ) {
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.TextFacet::class)
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
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_SET_FACET_VALUE_PARAMETER) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderUsingAndReturningGenericParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderThrowingException {
        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doThrowAnException(
            @ProvideBuilderData data: BuilderDataProviderThrowingException,
        )

        @Suppress("UNUSED")
        @BuilderDataProvider
        class BuilderDataProviderThrowingException {
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.TextFacet::class)
            fun getText(): String {
                throw NoSuchElementException("This facet value is not available.")
            }
        }
    }

    @Test
    fun `test builder data provider method throwing an exception should fail with this exception`() {
        val builderDataProvider = BuilderMethodDataProviderThrowingException.BuilderDataProviderThrowingException()
        val exception = assertThrows<DataProviderInvocationRuntimeException> {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderThrowingException::class) { builder ->
                    builder.doThrowAnException(builderDataProvider)
                }
            }
        }

        assertEquals("This facet value is not available.", exception.cause?.message)
    }
}