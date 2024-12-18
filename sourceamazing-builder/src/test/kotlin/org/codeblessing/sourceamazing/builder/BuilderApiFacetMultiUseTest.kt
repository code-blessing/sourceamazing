package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test

class BuilderApiFacetMultiUseTest {
    @Schema(concepts = [
        SchemaWithConceptWithMultiUsedFacet.ConceptWithFacetAlphaAndBeta::class,
        SchemaWithConceptWithMultiUsedFacet.ConceptWithFacetAlpha::class,
        SchemaWithConceptWithMultiUsedFacet.ConceptWithFacetBeta::class,
    ])
    private interface SchemaWithConceptWithMultiUsedFacet {

        @Concept(facets = [
            FacetAlpha::class,
            FacetBeta::class,
        ])
        interface ConceptWithFacetAlphaAndBeta

        @Concept(facets = [
            FacetAlpha::class,
        ])
        interface ConceptWithFacetAlpha

        @Concept(facets = [
            FacetBeta::class,
        ])
        interface ConceptWithFacetBeta

        @StringFacet
        interface FacetAlpha

        @StringFacet
        interface FacetBeta

    }

    @Builder
    private interface BuilderMethodUsingSameBuilderForDifferentConceptsWithOverlappingFacets {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithMultiUsedFacet.ConceptWithFacetAlphaAndBeta::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomethingConceptWithFacetAlphaAndBeta(): NestedBuilder

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithMultiUsedFacet.ConceptWithFacetAlpha::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomethingWithConceptWithFacetAlpha(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            fun doSomethingWithFacetAlpha(
                @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithMultiUsedFacet.FacetAlpha::class) myValue: String
            )
        }
    }

    @Test
    fun `test using nested builder for two different concepts with overlapping facets should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithMultiUsedFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodUsingSameBuilderForDifferentConceptsWithOverlappingFacets::class) { 
                // do nothing
            }
        }
    }
    @Builder
    private interface BuilderMethodUsingSameBuilderForDifferentConceptsWithoutOverlappingFacets {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithMultiUsedFacet.ConceptWithFacetAlphaAndBeta::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomethingConceptWithFacetAlphaAndBeta(): NestedBuilder

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithMultiUsedFacet.ConceptWithFacetBeta::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomethingWithConceptWithFacetBeta(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            fun doSomethingWithFacetAlpha(
                @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithMultiUsedFacet.FacetAlpha::class) myValue: String
            )
        }
    }

    @Test
    fun `test using nested builder for two different concepts without overlapping facets should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_FACET) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithMultiUsedFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodUsingSameBuilderForDifferentConceptsWithoutOverlappingFacets::class) { 
                    // do nothing
                }
            }
        }
    }
}