package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderDataProvider
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreProvidedNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.ProvideBuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test
import java.util.*

class BuilderMethodApiParameterTypesTest {
    enum class MyEnum {
        @Suppress("UNUSED") A,
        @Suppress("UNUSED") B,
        @Suppress("UNUSED") C,
    }

    enum class MyExactSameEnum {
        @Suppress("UNUSED") A,
        @Suppress("UNUSED") B,
        @Suppress("UNUSED") C,
    }

    enum class MySubsetEnum {
        @Suppress("UNUSED") A,
        @Suppress("UNUSED") C,
    }

    enum class MyOtherIncompatibleEnum {
        @Suppress("UNUSED") A,
        @Suppress("UNUSED") B,
        @Suppress("UNUSED") C,
        @Suppress("UNUSED") D,
    }


    @Schema(concepts = [SchemaWithConceptWithFacets.ConceptWithFacets::class])
    private interface SchemaWithConceptWithFacets {
        @Concept(facets = [
            ConceptWithFacets.TextFacet::class,
            ConceptWithFacets.OtherTextFacet::class,
            ConceptWithFacets.BoolFacet::class,
            ConceptWithFacets.NumberFacet::class,
            ConceptWithFacets.EnumerationFacet::class,
            ConceptWithFacets.SelfRefFacet::class,
        ])
        interface ConceptWithFacets {
            @StringFacet
            interface TextFacet
            @StringFacet
            interface OtherTextFacet
            @BooleanFacet
            interface BoolFacet
            @IntFacet
            interface NumberFacet
            @EnumFacet(MyEnum::class)
            interface EnumerationFacet
            @ReferenceFacet([ConceptWithFacets::class])
            interface SelfRefFacet
        }
    }

    @Builder
    private interface BuilderMethodWithIllegalConceptIdClass {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptId: String,
        )
    }

    @Test
    fun `test concept id parameter without ConceptIdentifier class should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithIllegalConceptIdClass::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithIllegalConceptIdClass {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedConceptIdentifierValue
            fun getConceptIdentifier(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test concept id parameter without ConceptIdentifier in data provider class should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithIllegalConceptIdClass::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableConceptId {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptId: ConceptIdentifier?,
        )
    }

    @Test
    fun `test concept id parameter as nullable type should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_TYPE_NO_NULLABLE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithNullableConceptId::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithNullableConceptId {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedConceptIdentifierValue
            fun getConceptIdentifier(): ConceptIdentifier? {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test concept id parameter as nullable type in data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_TYPE_NO_NULLABLE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithNullableConceptId::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableSetConceptIdAndIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        fun doSomething(
            @IgnoreNullFacetValue @SetConceptIdentifierValue conceptId: ConceptIdentifier?,
        )
    }

    @Test
    fun `test concept id parameter as nullable type with IgnoreNullFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithNullableSetConceptIdAndIgnoreNullFacetValueAnnotation::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithNullableSetConceptIdAndIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedConceptIdentifierValue
            @IgnoreProvidedNullFacetValue
            fun getConceptIdentifier(): ConceptIdentifier? {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test concept id parameter as nullable type with IgnoreNullFacetValue annotation in data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithNullableSetConceptIdAndIgnoreNullFacetValueAnnotation::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithSetConceptIdentifierAndIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        fun doSomething(
            @IgnoreNullFacetValue @SetConceptIdentifierValue conceptId: ConceptIdentifier,
        )
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and ConceptIdentifierValue annotation on same method should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodParamWithSetConceptIdentifierAndIgnoreNullFacetValueAnnotation::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithSetConceptIdentifierAndIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedConceptIdentifierValue
            @IgnoreProvidedNullFacetValue
            fun getConceptIdentifier(): ConceptIdentifier {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and ConceptIdentifierValue annotation on same method in data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithSetConceptIdentifierAndIgnoreNullFacetValueAnnotation::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableParameterWithoutIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class) myText: String?,
        )
    }

    @Test
    fun `test string facet parameter as nullable type without IgnoreNullFacetValue should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithNullableParameterWithoutIgnoreNullFacetValueAnnotation::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithNullableParameterWithoutIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class)
            fun getMyText(): String? {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter as nullable type without IgnoreNullFacetValue in data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithNullableParameterWithoutIgnoreNullFacetValueAnnotation::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableParameterWithIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @IgnoreNullFacetValue
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class)
            myText: String?,
        )
    }

    @Test
    fun `test string facet parameter as nullable type with IgnoreNullFacetValue should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithNullableParameterWithIgnoreNullFacetValueAnnotation::class) { 
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithNullableParameterWithIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class)
            @IgnoreProvidedNullFacetValue
            fun getMyText(): String? {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter as nullable type with IgnoreNullFacetValue on data provider should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithNullableParameterWithIgnoreNullFacetValueAnnotation::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCollectionTypedStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class) myTexts: List<String>,
        )
    }

    @Test
    fun `test string facet parameter with collection type instead of string should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithCollectionTypedStringParameter::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithCollectionTypedStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class)
            fun getMyTexts(): List<String> {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter with collection type instead of string on data provider should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithCollectionTypedStringParameter::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithMultipleSetFacetValueOnSameFacetMethod {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class) myText1: String,
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class) myText2: String,
        )
    }

    @Test
    fun `test multiple assignments via parameter value for the same facet should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithMultipleSetFacetValueOnSameFacetMethod::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithMultipleSetFacetValueOnSameFacetMethod {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class)
            fun getMyText1(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class)
            fun getMyText2(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }

        }
    }

    @Test
    fun `test multiple assignments via data provider for the same facet should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithMultipleSetFacetValueOnSameFacetMethod::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithSameFacetValueOnMultipleFacetMethod {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class)
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.OtherTextFacet::class)
            fun getMyText(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test multiple assignments via the same data provider method for different facets should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithSameFacetValueOnMultipleFacetMethod::class) {
                // do nothing
            }
        }
    }


    @Builder
    private interface BuilderMethodWithSortedSetStringCollectionParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class) myTexts: SortedSet<String>,
        )
    }

    @Test
    fun `test string facet parameter with SortedSet collection of string should throw an exception as only List, Set and Array are supported`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithSortedSetStringCollectionParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithSortedSetStringCollectionParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class)
            fun getSortedTexts(): SortedSet<String> {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter with SortedSet collection of string on data provider should throw an exception as only List, Set and Array are supported`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithSortedSetStringCollectionParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCollectionTypedNullableStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class) myTexts: List<String?>,
        )
    }

    @Test
    fun `test string facet parameter with collection type of nullable string should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithCollectionTypedNullableStringParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithCollectionTypedNullableStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class)
            fun getMyTexts(): List<String?> {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter with collection type of nullable string on data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithCollectionTypedNullableStringParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCollectionTypedNullableStringParameterWithIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @IgnoreNullFacetValue @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class) myTexts: List<String?>,
        )
    }

    @Test
    fun `test string facet parameter with collection type of nullable string and IgnoreNullFacetValue annotation should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithCollectionTypedNullableStringParameterWithIgnoreNullFacetValueAnnotation::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithCollectionTypedNullableStringParameterWithIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class)
            @IgnoreProvidedNullFacetValue
            fun getMyTexts(): List<String?> {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter with collection type of nullable string and IgnoreNullFacetValue annotation on data provider should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithCollectionTypedNullableStringParameterWithIgnoreNullFacetValueAnnotation::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class) myText: Int,
        )
    }

    @Test
    fun `test string facet parameter with other type than string should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongTypedStringParameter::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithWrongTypedStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class)
            fun getMyText(): Int {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter with other type than string on data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithWrongTypedStringParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongFunctionReturningStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class) myText: () -> String,
        )
    }

    @Test
    fun `test string facet parameter with a function returning a string should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_SET_FACET_VALUE_PARAMETER) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongFunctionReturningStringParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithWrongFunctionReturningStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class)
            fun getMyText(): () -> String {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter with a function returning a string on data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_SET_FACET_VALUE_PARAMETER) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithWrongFunctionReturningStringParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedBooleanParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.BoolFacet::class) myBoolean: Int,
        )
    }

    @Test
    fun `test boolean facet parameter with other type than boolean should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_BOOLEAN_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongTypedBooleanParameter::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithArrayOfBooleanParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.BoolFacet::class) myBooleans: Array<Boolean>,
        )
    }

    @Test
    fun `test boolean facet parameter with array of boolean instead of boolean should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithArrayOfBooleanParameter::class) { 
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithArrayOfBooleanParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.BoolFacet::class)
            fun getMyBooleans(): Array<Boolean> {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test boolean facet parameter with array of boolean instead of boolean with data provider should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithArrayOfBooleanParameter::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedIntParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.NumberFacet::class) myInt: String,
        )
    }

    @Test
    fun `test int facet parameter with other type than int should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongTypedIntParameter::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithLongInsteadOfIntParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.NumberFacet::class) myInt: Long,
        )
    }

    @Test
    fun `test int facet parameter with long type should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithLongInsteadOfIntParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithLongInsteadOfIntParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.NumberFacet::class)
            fun getMyInt(): Long {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test int facet parameter with long type on data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithLongInsteadOfIntParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNumberParameterInsteadOfIntParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.NumberFacet::class) myInt: Number,
        )
    }

    @Test
    fun `test int facet parameter with Number type should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithNumberParameterInsteadOfIntParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithNumberParameterInsteadOfIntParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.NumberFacet::class)
            fun getMyInt(): Number {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test int facet parameter with Number type on data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithNumberParameterInsteadOfIntParameter::class) {
                    // do nothing
                }
            }
        }
    }


    @Builder
    private interface BuilderMethodWithAnyParameterInsteadOfIntParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.NumberFacet::class) myInt: Any,
        )
    }

    @Test
    fun `test int facet parameter with Any type should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithAnyParameterInsteadOfIntParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithIntInsteadOfEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.EnumerationFacet::class) myEnum: Int,
        )
    }

    @Test
    fun `test enum facet parameter with wrong int type instead of enum should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithIntInsteadOfEnumParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithOtherCompatibleEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.EnumerationFacet::class) myEnum: MySubsetEnum,
        )
    }

    @Test
    fun `test enum facet parameter with other compatible enum instead of same enum should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithOtherCompatibleEnumParameter::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithOtherCompatibleEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.EnumerationFacet::class)
            fun getMyEnum(): MySubsetEnum {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test enum facet parameter with other compatible enum instead of same enum on data provider should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithOtherCompatibleEnumParameter::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithExactSameEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.EnumerationFacet::class) myEnum: MyExactSameEnum,
        )
    }

    @Test
    fun `test enum facet parameter with exact copy of enum instead of same enum should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithExactSameEnumParameter::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithOtherIncompatibleEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.EnumerationFacet::class) myEnum: MyOtherIncompatibleEnum,
        )
    }

    @Test
    fun `test enum facet parameter with other incompatible enum instead of correct enum should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithOtherIncompatibleEnumParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithStringInsteadOfEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.EnumerationFacet::class) myEnum: String,
        )
    }

    @Test
    fun `test enum facet parameter with string instead of enum should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithStringInsteadOfEnumParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithStringInsteadOfEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.EnumerationFacet::class)
            fun getMyEnum(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test enum facet parameter with string instead of enum on data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithStringInsteadOfEnumParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCorrectEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.EnumerationFacet::class) myEnum: MyEnum,
        )
    }

    @Test
    fun `test enum facet parameter should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithCorrectEnumParameter::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithSetOfEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.EnumerationFacet::class) myEnums: Set<MyEnum>,
        )
    }

    @Test
    fun `test enum facet parameter with set of enum instead of single enum should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithSetOfEnumParameter::class) { 
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableSetOfEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.EnumerationFacet::class) myEnums: Set<MyEnum>?,
        )
    }

    @Test
    fun `test enum facet parameter with nullable set of enum instead of single enum should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_NO_NULLABLE_COLLECTION_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithNullableSetOfEnumParameter::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedReferenceParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.SelfRefFacet::class) myRef: String,
        )
    }

    @Test
    fun `test reference facet parameter with other type than a ConceptIdentifier should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_REFERENCE_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongTypedReferenceParameter::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithWrongTypedReferenceParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.SelfRefFacet::class)
            fun getMyRef(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test reference facet parameter with other type than a ConceptIdentifier on data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_REFERENCE_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithWrongTypedReferenceParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithVarargArray {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.SelfRefFacet::class) vararg myRefs: ConceptIdentifier,
        )
    }

    @Test
    fun `test reference facet parameter with vararg array of ConceptIdentifier should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithVarargArray::class) { 
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.SelfRefFacet::class) myRef: ConceptIdentifier,
        )
    }

    @Test
    fun `test reference facet parameter with correct ConceptIdentifier type should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithConceptIdentifier::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.SelfRefFacet::class)
            fun getMyRef(): ConceptIdentifier {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test reference facet parameter with correct ConceptIdentifier type on data provider should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodDataProviderWithConceptIdentifier::class) {
                // do nothing
            }
        }
    }
}