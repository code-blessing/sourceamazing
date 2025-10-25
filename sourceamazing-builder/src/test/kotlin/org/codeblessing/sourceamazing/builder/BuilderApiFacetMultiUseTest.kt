package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test

class BuilderApiFacetMultiUseTest {
    private interface SchemaWithConceptWithMultiUsedFacet {

        interface ConceptWithFacetAlphaAndBeta {
            @Suppress("UNUSED")
            @Facet
            val facetAlpha: String
            @Suppress("UNUSED")
            @Facet
            val facetBeta: String
        }

        interface ConceptWithFacetAlpha {
            @Suppress("UNUSED")
            @Facet
            val facetAlpha: String
        }

        interface ConceptWithFacetBeta {
            @Suppress("UNUSED")
            @Facet
            val facetBeta: String
        }

        @Suppress("UNUSED")
        @Facet
        val conceptAlphaAndBeta: ConceptWithFacetAlphaAndBeta
        @Suppress("UNUSED")
        @Facet
        val conceptAlpha: ConceptWithFacetAlpha
        @Suppress("UNUSED")
        @Facet
        val conceptBeta: ConceptWithFacetBeta
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
                @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "facetAlpha") myValue: String
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
                @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "facetAlpha") myValue: String
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
