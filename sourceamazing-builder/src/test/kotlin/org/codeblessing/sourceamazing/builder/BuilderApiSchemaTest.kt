package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedStringFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test

class BuilderApiSchemaTest {
    @Schema(concepts = [
        SchemaWithConceptWithFacet.KnownConceptWithFacet::class,
        SchemaWithConceptWithFacet.AlsoKnownConceptWithFacet::class,
    ])
    private interface SchemaWithConceptWithFacet {

        @Concept(facets = [
            KnownConceptWithFacet.KnownFacet::class,
        ])
        interface KnownConceptWithFacet {
            @StringFacet
            interface KnownFacet
        }

        @Concept(facets = [
            AlsoKnownConceptWithFacet.AlsoKnownFacet::class,
        ])
        interface AlsoKnownConceptWithFacet {
            @StringFacet
            interface AlsoKnownFacet
        }

        @Concept(facets = [
            UnknownConceptWithFacet.UnknownFacet::class,
        ])
        interface UnknownConceptWithFacet {
            @StringFacet
            interface UnknownFacet
        }
    }

    @Builder
    private interface BuilderMethodCreatingKnownConcept {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.KnownConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test using NewConcept annotation with known concept should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodCreatingKnownConcept::class) { 
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodCreatingUnknownConcept {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.UnknownConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test using NewConcept annotation with unknown concept should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_CONCEPT) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodCreatingUnknownConcept::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingUnknownFacetAsParameterValue {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.KnownConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.UnknownConceptWithFacet.UnknownFacet::class) myValue: String
        )
    }

    @Test
    fun `test using unknown facet as parameter value of known concept should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_FACET) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodUsingUnknownFacetAsParameterValue::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingUnknownFacetAsFixedValue {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.KnownConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetFixedStringFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.UnknownConceptWithFacet.UnknownFacet::class, value = "hello")
        fun doSomething()
    }

    @Test
    fun `test using unknown facet as fixed value of known concept should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_FACET) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodUsingUnknownFacetAsFixedValue::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingFacetOfAnotherKnownConceptAsParameterValue {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.KnownConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.AlsoKnownConceptWithFacet.AlsoKnownFacet::class) myValue: String
        )
    }

    @Test
    fun `test using known facet of another concept as parameter value should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_FACET) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodUsingFacetOfAnotherKnownConceptAsParameterValue::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingFacetOfAnotherKnownConceptAsFixedValue {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.KnownConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetFixedStringFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.AlsoKnownConceptWithFacet.AlsoKnownFacet::class, value = "hello")
        fun doSomething()
    }

    @Test
    fun `test using known facet of another concept as fixed value should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_FACET) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodUsingFacetOfAnotherKnownConceptAsFixedValue::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodPassingCorrectConceptParentNestedBuilderAsParameterValue {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.KnownConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            fun doSomethingNested(
                @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.KnownConceptWithFacet.KnownFacet::class) myValue: String
            )
        }
    }

    @Test
    fun `test using known facet of correct concept as parameter value in nested builder should throw an exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodPassingCorrectConceptParentNestedBuilderAsParameterValue::class) { 
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodPassingCorrectConceptParentNestedBuilderAsFixedValue {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.KnownConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            @SetFixedStringFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.KnownConceptWithFacet.KnownFacet::class, value = "hello")
            fun doSomethingNested()
        }
    }

    @Test
    fun `test using known facet of correct concept as fixed value in nested builder should throw an exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodPassingCorrectConceptParentNestedBuilderAsFixedValue::class) { 
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodPassingWrongConceptParentNestedBuilderAsParameterValue {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.KnownConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            fun doSomethingWrongFacet(
                @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.AlsoKnownConceptWithFacet.AlsoKnownFacet::class) myValue: String
            )

            @Suppress("UNUSED")
            @BuilderMethod
            fun doSomethingCorrectFacet(
                @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.KnownConceptWithFacet.KnownFacet::class) myValue: String
            )

        }
    }

    @Test
    fun `test using known facet of wrong concept as parameter value in nested builder should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_FACET) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodPassingWrongConceptParentNestedBuilderAsParameterValue::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodPassingWrongConceptParentNestedBuilderAsFixedValue {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.KnownConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            @SetFixedStringFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.AlsoKnownConceptWithFacet.AlsoKnownFacet::class, value = "hello")
            fun doSomethingWrongFacet()

            @Suppress("UNUSED")
            @BuilderMethod
            @SetFixedStringFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.KnownConceptWithFacet.KnownFacet::class, value = "hello")
            fun doSomethingCorrectFacet()

        }
    }

    @Test
    fun `test using known facet of wrong concept as fixed value in nested builder should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_FACET) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodPassingWrongConceptParentNestedBuilderAsFixedValue::class) { 
                    // do nothing
                }
            }
        }
    }

}