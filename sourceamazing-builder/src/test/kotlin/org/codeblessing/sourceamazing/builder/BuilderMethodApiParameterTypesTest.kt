package org.codeblessing.sourceamazing.builder

import java.util.*
import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderMethodApiParameterTypesTest {
    enum class MyEnum {
        A,
        B,
        C,
    }

    enum class MyExactSameEnum {
        A,
        B,
        C,
    }

    enum class MySubsetEnum {
        A,
        C,
    }

    enum class MyOtherIncompatibleEnum {
        A,
        B,
        C,
        D,
    }

    private interface MyConcepts {
        interface MyConcept {
            val textFacet: String

            val otherTextFacet: String

            val boolFacet: Boolean

            val numberFacet: Int

            val enumerationFacet: MyEnum

            val selfRefFacet: MyConcept
        }

        val concepts: List<MyConcept>
    }

    @Builder
    private interface BuilderMethodWithIllegalConceptIdClass {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(@SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptId: String)
    }

    @Test
    fun `test concept id parameter without ConceptIdentifier class should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithIllegalConceptIdClass::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithIllegalConceptIdClass {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun getConceptIdentifier(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test concept id parameter without ConceptIdentifier in data provider class should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderWithIllegalConceptIdClass::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableConceptId {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptId: ConceptIdentifier?
        )
    }

    @Test
    fun `test concept id parameter as nullable type should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_TYPE_NO_NULLABLE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithNullableConceptId::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithNullableConceptId {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun getConceptIdentifier(): ConceptIdentifier? {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test concept id parameter as nullable type in data provider should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_TYPE_NO_NULLABLE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderWithNullableConceptId::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableSetConceptIdAndIgnoreNullFacetValueAnnotation {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(
            @IgnoreNullFacetValue
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo")
            conceptId: ConceptIdentifier?
        )
    }

    @Test
    fun `test concept id parameter as nullable type with IgnoreNullFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithNullableSetConceptIdAndIgnoreNullFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithNullableSetConceptIdAndIgnoreNullFacetValueAnnotation {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedConceptIdentifierValue(conceptToModifyAlias = "foo")
            @IgnoreProvidedNullFacetValue
            fun getConceptIdentifier(): ConceptIdentifier? {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test concept id parameter as nullable type with IgnoreNullFacetValue annotation in data provider should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderWithNullableSetConceptIdAndIgnoreNullFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithSetConceptIdentifierAndIgnoreNullFacetValueAnnotation {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(
            @IgnoreNullFacetValue
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo")
            conceptId: ConceptIdentifier
        )
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and ConceptIdentifierValue annotation on same method should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodParamWithSetConceptIdentifierAndIgnoreNullFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithSetConceptIdentifierAndIgnoreNullFacetValueAnnotation {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedConceptIdentifierValue(conceptToModifyAlias = "foo")
            @IgnoreProvidedNullFacetValue
            fun getConceptIdentifier(): ConceptIdentifier {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and ConceptIdentifierValue annotation on same method in data provider should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderWithSetConceptIdentifierAndIgnoreNullFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableParameterWithoutIgnoreNullFacetValueAnnotation {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            myText: String?
        )
    }

    @Test
    fun `test string facet parameter as nullable type without IgnoreNullFacetValue should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithNullableParameterWithoutIgnoreNullFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithNullableParameterWithoutIgnoreNullFacetValueAnnotation {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            fun getMyText(): String? {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter as nullable type without IgnoreNullFacetValue in data provider should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderWithNullableParameterWithoutIgnoreNullFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableParameterWithIgnoreNullFacetValueAnnotation {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @IgnoreNullFacetValue
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            myText: String?
        )
    }

    @Test
    fun `test string facet parameter as nullable type with IgnoreNullFacetValue should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithNullableParameterWithIgnoreNullFacetValueAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithNullableParameterWithIgnoreNullFacetValueAnnotation {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            @IgnoreProvidedNullFacetValue
            fun getMyText(): String? {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter as nullable type with IgnoreNullFacetValue on data provider should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodDataProviderWithNullableParameterWithIgnoreNullFacetValueAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCollectionTypedStringParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            myTexts: List<String>
        )
    }

    @Test
    fun `test string facet parameter with collection type instead of string should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithCollectionTypedStringParameter::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithCollectionTypedStringParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            fun getMyTexts(): List<String> {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter with collection type instead of string on data provider should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodDataProviderWithCollectionTypedStringParameter::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithMultipleSetFacetValueOnSameFacetMethod {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            myText1: String,
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            myText2: String,
        )
    }

    @Test
    fun `test multiple assignments via parameter value for the same facet should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithMultipleSetFacetValueOnSameFacetMethod::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithMultipleSetFacetValueOnSameFacetMethod {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            fun getMyText1(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            fun getMyText2(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test multiple assignments via data provider for the same facet should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodDataProviderWithMultipleSetFacetValueOnSameFacetMethod::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithSameFacetValueOnMultipleFacetMethod {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "otherTextFacet")
            fun getMyText(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test multiple assignments via the same data provider method for different facets should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodDataProviderWithSameFacetValueOnMultipleFacetMethod::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithSortedSetStringCollectionParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            myTexts: SortedSet<String>
        )
    }

    @Test
    fun `test string facet parameter with SortedSet collection of string should throw an exception as only List, Set and Array are supported`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithSortedSetStringCollectionParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithSortedSetStringCollectionParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            fun getSortedTexts(): SortedSet<String> {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter with SortedSet collection of string on data provider should throw an exception as only List, Set and Array are supported`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderWithSortedSetStringCollectionParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCollectionTypedNullableStringParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            myTexts: List<String?>
        )
    }

    @Test
    fun `test string facet parameter with collection type of nullable string should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithCollectionTypedNullableStringParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithCollectionTypedNullableStringParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            fun getMyTexts(): List<String?> {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter with collection type of nullable string on data provider should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderWithCollectionTypedNullableStringParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCollectionTypedNullableStringParameterWithIgnoreNullFacetValueAnnotation {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @IgnoreNullFacetValue
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            myTexts: List<String?>
        )
    }

    @Test
    fun `test string facet parameter with collection type of nullable string and IgnoreNullFacetValue annotation should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithCollectionTypedNullableStringParameterWithIgnoreNullFacetValueAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithCollectionTypedNullableStringParameterWithIgnoreNullFacetValueAnnotation {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            @IgnoreProvidedNullFacetValue
            fun getMyTexts(): List<String?> {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter with collection type of nullable string and IgnoreNullFacetValue annotation on data provider should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodDataProviderWithCollectionTypedNullableStringParameterWithIgnoreNullFacetValueAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedStringParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet") myText: Int
        )
    }

    @Test
    fun `test string facet parameter with other type than string should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithWrongTypedStringParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithWrongTypedStringParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            fun getMyText(): Int {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter with other type than string on data provider should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderWithWrongTypedStringParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongFunctionReturningStringParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            myText: () -> String
        )
    }

    @Test
    fun `test string facet parameter with a function returning a string should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_FACET_VALUE_PARAMETER,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithWrongFunctionReturningStringParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithWrongFunctionReturningStringParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            fun getMyText(): () -> String {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter with a function returning a string on data provider should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_FACET_VALUE_PARAMETER,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderWithWrongFunctionReturningStringParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedBooleanParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "boolFacet") myBoolean: Int
        )
    }

    @Test
    fun `test boolean facet parameter with other type than boolean should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_BOOLEAN_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithWrongTypedBooleanParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithArrayOfBooleanParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "boolFacet")
            myBooleans: Array<Boolean>
        )
    }

    @Test
    fun `test boolean facet parameter with array of boolean instead of boolean should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithArrayOfBooleanParameter::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithArrayOfBooleanParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "boolFacet")
            fun getMyBooleans(): Array<Boolean> {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test boolean facet parameter with array of boolean instead of boolean with data provider should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodDataProviderWithArrayOfBooleanParameter::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedIntParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "numberFacet")
            myInt: String
        )
    }

    @Test
    fun `test int facet parameter with other type than int should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithWrongTypedIntParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithLongInsteadOfIntParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "numberFacet") myInt: Long
        )
    }

    @Test
    fun `test int facet parameter with long type should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithLongInsteadOfIntParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithLongInsteadOfIntParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "numberFacet")
            fun getMyInt(): Long {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test int facet parameter with long type on data provider should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderWithLongInsteadOfIntParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNumberParameterInsteadOfIntParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "numberFacet")
            myInt: Number
        )
    }

    @Test
    fun `test int facet parameter with Number type should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithNumberParameterInsteadOfIntParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithNumberParameterInsteadOfIntParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "numberFacet")
            fun getMyInt(): Number {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test int facet parameter with Number type on data provider should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderWithNumberParameterInsteadOfIntParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAnyParameterInsteadOfIntParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "numberFacet") myInt: Any
        )
    }

    @Test
    fun `test int facet parameter with Any type should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithAnyParameterInsteadOfIntParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithIntInsteadOfEnumParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet")
            myEnum: Int
        )
    }

    @Test
    fun `test enum facet parameter with wrong int type instead of enum should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithIntInsteadOfEnumParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithOtherCompatibleEnumParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet")
            myEnum: MySubsetEnum
        )
    }

    @Test
    fun `test enum facet parameter with other compatible enum instead of same enum should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithOtherCompatibleEnumParameter::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithOtherCompatibleEnumParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet")
            fun getMyEnum(): MySubsetEnum {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test enum facet parameter with other compatible enum instead of same enum on data provider should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodDataProviderWithOtherCompatibleEnumParameter::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithExactSameEnumParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet")
            myEnum: MyExactSameEnum
        )
    }

    @Test
    fun `test enum facet parameter with exact copy of enum instead of same enum should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithExactSameEnumParameter::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithOtherIncompatibleEnumParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet")
            myEnum: MyOtherIncompatibleEnum
        )
    }

    @Test
    fun `test enum facet parameter with other incompatible enum instead of correct enum should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithOtherIncompatibleEnumParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithStringInsteadOfEnumParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet")
            myEnum: String
        )
    }

    @Test
    fun `test enum facet parameter with string instead of enum should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithStringInsteadOfEnumParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithStringInsteadOfEnumParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet")
            fun getMyEnum(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test enum facet parameter with string instead of enum on data provider should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderWithStringInsteadOfEnumParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCorrectEnumParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet")
            myEnum: MyEnum
        )
    }

    @Test
    fun `test enum facet parameter should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithCorrectEnumParameter::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithSetOfEnumParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet")
            myEnums: Set<MyEnum>
        )
    }

    @Test
    fun `test enum facet parameter with set of enum instead of single enum should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithSetOfEnumParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableSetOfEnumParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet")
            myEnums: Set<MyEnum>?
        )
    }

    @Test
    fun `test enum facet parameter with nullable set of enum instead of single enum should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_NO_NULLABLE_COLLECTION_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithNullableSetOfEnumParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedReferenceParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet")
            myRef: String
        )
    }

    @Test
    fun `test reference facet parameter with other type than a ConceptIdentifier should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_REFERENCE_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithWrongTypedReferenceParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithWrongTypedReferenceParameter {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet")
            fun getMyRef(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test reference facet parameter with other type than a ConceptIdentifier on data provider should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_REFERENCE_FACET_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodDataProviderWithWrongTypedReferenceParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithVarargArray {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet")
            vararg myRefs: ConceptIdentifier
        )
    }

    @Test
    fun `test reference facet parameter with vararg array of ConceptIdentifier should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithVarargArray::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithConceptIdentifier {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet")
            myRef: ConceptIdentifier
        )
    }

    @Test
    fun `test reference facet parameter with correct ConceptIdentifier type should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithConceptIdentifier::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodDataProviderWithConceptIdentifier {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet")
            fun getMyRef(): ConceptIdentifier {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test reference facet parameter with correct ConceptIdentifier type on data provider should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodDataProviderWithConceptIdentifier::class,
                ) {
                    // do nothing
                }
            }
        }
    }
}
