package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Test
import java.util.*

class BuilderMethodApiParameterTypesTest {
    enum class MyEnum {
        @Suppress("UNUSED")
        A,

        @Suppress("UNUSED")
        B,

        @Suppress("UNUSED")
        C,
    }

    enum class MyExactSameEnum {
        @Suppress("UNUSED")
        A,

        @Suppress("UNUSED")
        B,

        @Suppress("UNUSED")
        C,
    }

    enum class MySubsetEnum {
        @Suppress("UNUSED")
        A,

        @Suppress("UNUSED")
        C,
    }

    enum class MyOtherIncompatibleEnum {
        @Suppress("UNUSED")
        A,

        @Suppress("UNUSED")
        B,

        @Suppress("UNUSED")
        C,

        @Suppress("UNUSED")
        D,
    }


    private interface SchemaWithConceptWithFacets {
        interface ConceptWithFacets {
            @Suppress("UNUSED")
            val textFacet: String

            @Suppress("UNUSED")
            val otherTextFacet: String

            @Suppress("UNUSED")
            val boolFacet: Boolean

            @Suppress("UNUSED")
            val numberFacet: Int

            @Suppress("UNUSED")
            val enumerationFacet: MyEnum

            @Suppress("UNUSED")
            val selfRefFacet: ConceptWithFacets
        }

        @Suppress("UNUSED")
        val concepts: List<ConceptWithFacets>
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithIllegalConceptIdClass {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptId: String,
        )
    }

    @Test
    fun `test concept id parameter without ConceptIdentifier class should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithIllegalConceptIdClass {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithNullableConceptId {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptId: ConceptIdentifier?,
        )
    }

    @Test
    fun `test concept id parameter as nullable type should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_TYPE_NO_NULLABLE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithNullableConceptId {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithNullableSetConceptIdAndIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @IgnoreNullFacetValue @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptId: ConceptIdentifier?,
        )
    }

    @Test
    fun `test concept id parameter as nullable type with IgnoreNullFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithNullableSetConceptIdAndIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodParamWithSetConceptIdentifierAndIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @IgnoreNullFacetValue @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptId: ConceptIdentifier,
        )
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and ConceptIdentifierValue annotation on same method should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithSetConceptIdentifierAndIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithNullableParameterWithoutIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet") myText: String?,
        )
    }

    @Test
    fun `test string facet parameter as nullable type without IgnoreNullFacetValue should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithNullableParameterWithoutIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithNullableParameterWithIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @IgnoreNullFacetValue
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            myText: String?,
        )
    }

    @Test
    fun `test string facet parameter as nullable type with IgnoreNullFacetValue should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithNullableParameterWithIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithCollectionTypedStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet") myTexts: List<String>,
        )
    }

    @Test
    fun `test string facet parameter with collection type instead of string should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithCollectionTypedStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            fun getMyTexts(): List<String> {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test string facet parameter with collection type instead of string on data provider should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithMultipleSetFacetValueOnSameFacetMethod {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet") myText1: String,
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet") myText2: String,
        )
    }

    @Test
    fun `test multiple assignments via parameter value for the same facet should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithMultipleSetFacetValueOnSameFacetMethod {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            fun getMyText1(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet")
            fun getMyText2(): String {
                throw UnsupportedOperationException("Never called in validation phase")
            }

        }
    }

    @Test
    fun `test multiple assignments via data provider for the same facet should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithSameFacetValueOnMultipleFacetMethod {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithSortedSetStringCollectionParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet") myTexts: SortedSet<String>,
        )
    }

    @Test
    fun `test string facet parameter with SortedSet collection of string should throw an exception as only List, Set and Array are supported`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithSortedSetStringCollectionParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithCollectionTypedNullableStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet") myTexts: List<String?>,
        )
    }

    @Test
    fun `test string facet parameter with collection type of nullable string should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithCollectionTypedNullableStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithCollectionTypedNullableStringParameterWithIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @IgnoreNullFacetValue @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet") myTexts: List<String?>,
        )
    }

    @Test
    fun `test string facet parameter with collection type of nullable string and IgnoreNullFacetValue annotation should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithCollectionTypedNullableStringParameterWithIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithWrongTypedStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet") myText: Int,
        )
    }

    @Test
    fun `test string facet parameter with other type than string should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithWrongTypedStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithWrongFunctionReturningStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet") myText: () -> String,
        )
    }

    @Test
    fun `test string facet parameter with a function returning a string should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_FACET_VALUE_PARAMETER,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithWrongFunctionReturningStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithWrongTypedBooleanParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "boolFacet") myBoolean: Int,
        )
    }

    @Test
    fun `test boolean facet parameter with other type than boolean should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_BOOLEAN_FACET_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithArrayOfBooleanParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "boolFacet") myBooleans: Array<Boolean>,
        )
    }

    @Test
    fun `test boolean facet parameter with array of boolean instead of boolean should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithArrayOfBooleanParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "boolFacet")
            fun getMyBooleans(): Array<Boolean> {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test boolean facet parameter with array of boolean instead of boolean with data provider should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithWrongTypedIntParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "numberFacet") myInt: String,
        )
    }

    @Test
    fun `test int facet parameter with other type than int should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithLongInsteadOfIntParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "numberFacet") myInt: Long,
        )
    }

    @Test
    fun `test int facet parameter with long type should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithLongInsteadOfIntParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithNumberParameterInsteadOfIntParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "numberFacet") myInt: Number,
        )
    }

    @Test
    fun `test int facet parameter with Number type should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithNumberParameterInsteadOfIntParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithAnyParameterInsteadOfIntParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "numberFacet") myInt: Any,
        )
    }

    @Test
    fun `test int facet parameter with Any type should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithIntInsteadOfEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet") myEnum: Int,
        )
    }

    @Test
    fun `test enum facet parameter with wrong int type instead of enum should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithOtherCompatibleEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet") myEnum: MySubsetEnum,
        )
    }

    @Test
    fun `test enum facet parameter with other compatible enum instead of same enum should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithOtherCompatibleEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet")
            fun getMyEnum(): MySubsetEnum {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test enum facet parameter with other compatible enum instead of same enum on data provider should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithExactSameEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet") myEnum: MyExactSameEnum,
        )
    }

    @Test
    fun `test enum facet parameter with exact copy of enum instead of same enum should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithOtherIncompatibleEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet") myEnum: MyOtherIncompatibleEnum,
        )
    }

    @Test
    fun `test enum facet parameter with other incompatible enum instead of correct enum should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithStringInsteadOfEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet") myEnum: String,
        )
    }

    @Test
    fun `test enum facet parameter with string instead of enum should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithStringInsteadOfEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithCorrectEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet") myEnum: MyEnum,
        )
    }

    @Test
    fun `test enum facet parameter should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithSetOfEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet") myEnums: Set<MyEnum>,
        )
    }

    @Test
    fun `test enum facet parameter with set of enum instead of single enum should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithSetOfEnumParameter::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithNullableSetOfEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "enumerationFacet") myEnums: Set<MyEnum>?,
        )
    }

    @Test
    fun `test enum facet parameter with nullable set of enum instead of single enum should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_NO_NULLABLE_COLLECTION_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithWrongTypedReferenceParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet") myRef: String,
        )
    }

    @Test
    fun `test reference facet parameter with other type than a ConceptIdentifier should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_REFERENCE_FACET_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithWrongTypedReferenceParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithVarargArray {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet") vararg myRefs: ConceptIdentifier,
        )
    }

    @Test
    fun `test reference facet parameter with vararg array of ConceptIdentifier should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithVarargArray::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet") myRef: ConceptIdentifier,
        )
    }

    @Test
    fun `test reference facet parameter with correct ConceptIdentifier type should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithConceptIdentifier::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodDataProviderWithConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProvider,
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet")
            fun getMyRef(): ConceptIdentifier {
                throw UnsupportedOperationException("Never called in validation phase")
            }
        }
    }

    @Test
    fun `test reference facet parameter with correct ConceptIdentifier type on data provider should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) { 
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
