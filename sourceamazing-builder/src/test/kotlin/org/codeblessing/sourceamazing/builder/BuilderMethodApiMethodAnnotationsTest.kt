package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderMethodApiMethodAnnotationsTest {

    private interface SchemaWithConceptWithFacets {
        enum class MyEnum {
            A,
            B,
        }

        interface ConceptWithFacets {
            val textFacet: String

            val boolFacet: Boolean

            val numberFacet: Int

            val enumerationFacet: MyEnum

            val selfRefFacet: ConceptWithFacets
        }

        val concepts: List<ConceptWithFacets>
    }

    @Builder
    private interface BuilderMethodWithFixedFacetValueAndParameterFacetValue {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetFixedStringFacetValue(
            conceptToModifyAlias = "foo",
            facetToModify = "textFacet",
            value = "fixed value",
        )
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "textFacet") myText: String
        )
    }

    @Test
    fun `test string facet as fixed value and as parameter should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithFixedFacetValueAndParameterFacetValue::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithMultipleFixedFacetValues {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetFixedStringFacetValue(
            conceptToModifyAlias = "foo",
            facetToModify = "textFacet",
            value = "fixed value 1",
        )
        @SetFixedStringFacetValue(
            conceptToModifyAlias = "foo",
            facetToModify = "textFacet",
            value = "fixed value 2",
        )
        fun doSomething()
    }

    @Test
    fun `test string facet with multiple fixed values should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithMultipleFixedFacetValues::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCorrectFixedEnumFacetValues {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetFixedEnumFacetValue(
            conceptToModifyAlias = "foo",
            facetToModify = "enumerationFacet",
            value = "A",
        )
        fun doSomething()
    }

    @Test
    fun `test enum facet with fixed values having correct enum should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithCorrectFixedEnumFacetValues::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongFixedEnumFacetValues {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetFixedEnumFacetValue(
            conceptToModifyAlias = "foo",
            facetToModify = "enumerationFacet",
            value = "NOT_A_NOR_B",
        )
        fun doSomething()
    }

    @Test
    fun `test enum facet with fixed values having unknown enum value should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.WRONG_FACET_ENUM_VALUE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithWrongFixedEnumFacetValues::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongFixedFacetType {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetFixedBooleanFacetValue(
            conceptToModifyAlias = "foo",
            facetToModify = "numberFacet",
            value = true,
        )
        fun doSomething()
    }

    @Test
    fun `test int facet with fixed boolean value should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.WRONG_FACET_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithWrongFixedFacetType::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUnregisteredFixedFacetType {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetFixedIntFacetValue(
            conceptToModifyAlias = "foo",
            facetToModify = "unregisteredFacet",
            value = 42,
        )
        fun doSomething()
    }

    @Test
    fun `test int facet with fixed value for unregistered facet should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.UNKNOWN_FACET,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithUnregisteredFixedFacetType::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithValidFixedReference {

        @BuilderMethod
        @NewConcept(
            concept = SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @NewConcept(
            concept = SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "bar",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "bar")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "foo",
            facetToModify = "selfRefFacet",
            referencedConceptAlias = "bar",
        )
        fun doSomething()
    }

    @Test
    fun `test referencing a concept with fixed alias should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithValidFixedReference::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithValidParameterReference {

        @BuilderMethod
        @NewConcept(
            concept = SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @NewConcept(
            concept = SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "bar",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "bar")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet")
            myReference: ConceptIdentifier
        )
    }

    @Test
    fun `test referencing a concept with parameter value should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacets>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithValidParameterReference::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithFunctionInsteadOfConceptIdentifierReference {

        @BuilderMethod
        @NewConcept(
            concept = SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @NewConcept(
            concept = SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "bar",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "bar")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet")
            myReference: () -> ConceptIdentifier
        )
    }

    @Test
    fun `test reference facet with function instead of ConceptIdentifier should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_FACET_VALUE_PARAMETER,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithFunctionInsteadOfConceptIdentifierReference::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithStringInsteadOfConceptIdentifierReference {

        @BuilderMethod
        @NewConcept(
            concept = SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @NewConcept(
            concept = SchemaWithConceptWithFacets.ConceptWithFacets::class,
            declareConceptAlias = "bar",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "bar")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet")
            myReference: String
        )
    }

    @Test
    fun `test reference facet with string instead of ConceptIdentifier should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_WRONG_REFERENCE_FACET_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacets::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacets>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithStringInsteadOfConceptIdentifierReference::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }
}
