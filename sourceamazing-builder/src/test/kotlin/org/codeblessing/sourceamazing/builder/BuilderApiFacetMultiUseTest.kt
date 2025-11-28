package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderApiFacetMultiUseTest {
    private interface MyConcepts {

        interface ConceptWithFacetAlphaAndBeta {
            val facetAlpha: String

            val facetBeta: String
        }

        interface ConceptWithFacetAlpha {
            val facetAlpha: String
        }

        interface ConceptWithFacetBeta {
            val facetBeta: String
        }

        val conceptAlphaAndBeta: List<ConceptWithFacetAlphaAndBeta>

        val conceptAlpha: List<ConceptWithFacetAlpha>

        val conceptBeta: List<ConceptWithFacetBeta>
    }

    @Builder
    private interface BuilderMethodUsingSameBuilderForDifferentConceptsWithOverlappingFacets {

        @BuilderMethod
        @NewConcept(MyConcepts.ConceptWithFacetAlphaAndBeta::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomethingConceptWithFacetAlphaAndBeta(): NestedBuilder

        @BuilderMethod
        @NewConcept(MyConcepts.ConceptWithFacetAlpha::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomethingWithConceptWithFacetAlpha(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(
            concept = MyConcepts.ConceptWithFacetAlphaAndBeta::class,
            conceptAlias = "foo",
        )
        private interface NestedBuilder {

            @BuilderMethod
            fun doSomethingWithFacetAlpha(
                @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "facetAlpha") myValue: String
            )
        }
    }

    @Test
    fun `test using nested builder for two different concepts with overlapping facets should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodUsingSameBuilderForDifferentConceptsWithOverlappingFacets::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingSameBuilderForDifferentConceptsWithoutOverlappingFacets {

        @BuilderMethod
        @NewConcept(MyConcepts.ConceptWithFacetAlphaAndBeta::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomethingConceptWithFacetAlphaAndBeta(): NestedBuilder

        @BuilderMethod
        @NewConcept(MyConcepts.ConceptWithFacetBeta::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomethingWithConceptWithFacetBeta(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(
            concept = MyConcepts.ConceptWithFacetAlphaAndBeta::class,
            conceptAlias = "foo",
        )
        private interface NestedBuilder {

            @BuilderMethod
            fun doSomethingWithFacetAlpha(
                @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "facetAlpha") myValue: String
            )
        }
    }

    @Test
    fun `test using nested builder for two different concepts without overlapping facets should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_FACET) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodUsingSameBuilderForDifferentConceptsWithoutOverlappingFacets::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }
}
