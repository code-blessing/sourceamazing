package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderApiSchemaTest {
    private interface SchemaWithConceptWithFacet {

        interface KnownConceptWithFacet {
            val knownFacet: String
        }

        interface AlsoKnownConceptWithFacet {
            val alsoKnownFacet: String
        }

        interface UnknownConceptWithFacet {
            val unknownFacet: String
        }

        val knownConcepts: List<KnownConceptWithFacet>

        val alsoKnownConcepts: List<AlsoKnownConceptWithFacet>
    }

    @Builder
    private interface BuilderMethodCreatingKnownConcept {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacet.KnownConceptWithFacet::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test using NewConcept annotation with known concept should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacet>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderMethodCreatingKnownConcept::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodCreatingUnknownConcept {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacet.UnknownConceptWithFacet::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test using NewConcept annotation with unknown concept should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.UNKNOWN_CONCEPT,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodCreatingUnknownConcept::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingUnknownFacetAsParameterValue {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacet.KnownConceptWithFacet::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "UnknownFacet")
            myValue: String
        )
    }

    @Test
    fun `test using unknown facet as parameter value of known concept should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.UNKNOWN_FACET,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodUsingUnknownFacetAsParameterValue::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingUnknownFacetAsFixedValue {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacet.KnownConceptWithFacet::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetFixedStringFacetValue(
            conceptToModifyAlias = "foo",
            facetToModify = "UnknownFacet",
            value = "hello",
        )
        fun doSomething()
    }

    @Test
    fun `test using unknown facet as fixed value of known concept should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.UNKNOWN_FACET,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodUsingUnknownFacetAsFixedValue::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingFacetOfAnotherKnownConceptAsParameterValue {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacet.KnownConceptWithFacet::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "AlsoKnownFacet")
            myValue: String
        )
    }

    @Test
    fun `test using known facet of another concept as parameter value should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.UNKNOWN_FACET,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodUsingFacetOfAnotherKnownConceptAsParameterValue::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingFacetOfAnotherKnownConceptAsFixedValue {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacet.KnownConceptWithFacet::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetFixedStringFacetValue(
            conceptToModifyAlias = "foo",
            facetToModify = "AlsoKnownFacet",
            value = "hello",
        )
        fun doSomething()
    }

    @Test
    fun `test using known facet of another concept as fixed value should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.UNKNOWN_FACET,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodUsingFacetOfAnotherKnownConceptAsFixedValue::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodPassingCorrectConceptParentNestedBuilderAsParameterValue {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacet.KnownConceptWithFacet::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @BuilderMethod
            fun doSomethingNested(
                @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "knownFacet")
                myValue: String
            )
        }
    }

    @Test
    fun `test using known facet of correct concept as parameter value in nested builder should throw an exception`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacet>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodPassingCorrectConceptParentNestedBuilderAsParameterValue::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodPassingCorrectConceptParentNestedBuilderAsFixedValue {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacet.KnownConceptWithFacet::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @BuilderMethod
            @SetFixedStringFacetValue(
                conceptToModifyAlias = "foo",
                facetToModify = "knownFacet",
                value = "hello",
            )
            fun doSomethingNested()
        }
    }

    @Test
    fun `test using known facet of correct concept as fixed value in nested builder should throw an exception`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacet>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodPassingCorrectConceptParentNestedBuilderAsFixedValue::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodPassingWrongConceptParentNestedBuilderAsParameterValue {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacet.KnownConceptWithFacet::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @BuilderMethod
            fun doSomethingWrongFacet(
                @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "alsoKnownFacet")
                myValue: String
            )

            @BuilderMethod
            fun doSomethingCorrectFacet(
                @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "knownFacet")
                myValue: String
            )
        }
    }

    @Test
    fun `test using known facet of wrong concept as parameter value in nested builder should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.UNKNOWN_FACET,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodPassingWrongConceptParentNestedBuilderAsParameterValue::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodPassingWrongConceptParentNestedBuilderAsFixedValue {

        @BuilderMethod
        @NewConcept(
            SchemaWithConceptWithFacet.KnownConceptWithFacet::class,
            declareConceptAlias = "foo",
        )
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @BuilderMethod
            @SetFixedStringFacetValue(
                conceptToModifyAlias = "foo",
                facetToModify = "AlsoKnownFacet",
                value = "hello",
            )
            fun doSomethingWrongFacet()

            @BuilderMethod
            @SetFixedStringFacetValue(
                conceptToModifyAlias = "foo",
                facetToModify = "KnownFacet",
                value = "hello",
            )
            fun doSomethingCorrectFacet()
        }
    }

    @Test
    fun `test using known facet of wrong concept as fixed value in nested builder should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.UNKNOWN_FACET,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodPassingWrongConceptParentNestedBuilderAsFixedValue::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }
}
