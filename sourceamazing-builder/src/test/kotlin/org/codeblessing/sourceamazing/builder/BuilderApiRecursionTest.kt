package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.codeblessing.sourceamazing.toConceptName
import org.junit.jupiter.api.Test

class BuilderApiRecursionTest {
    private interface SchemaWithConcept {

        interface OneConcept

        @Suppress("UNUSED")
        @Facet
        val concepts: List<OneConcept>
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodUsingSameBuilderIndirectly {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConcept.OneConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        @WithNewBuilder(OuterNestedBuilder::class)
        fun doSomething(): OuterNestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface OuterNestedBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            @WithNewBuilder(InnerNestedBuilder::class)
            fun doSomething(): InnerNestedBuilder

        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface InnerNestedBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            @WithNewBuilder(OuterNestedBuilder::class)
            fun doSomething(): OuterNestedBuilder

        }

    }

    @Test
    fun `test using nested builder returning another inner nested builder that returns the first nested builder should not fail with a stack overflow`() {
        SchemaApi.withSchema(SchemaWithConcept::class) { schemaContext ->
            withRootInstance<SchemaWithConcept>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderMethodUsingSameBuilderIndirectly::class,
                ) {
                    // do nothing
                }
            }
        }
    }


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodUsingSameBuilderDirectly {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConcept.OneConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            @WithNewBuilder(NestedBuilder::class)
            fun doSomething(): NestedBuilder

        }
    }

    @Test
    fun `test using nested builder returning itself should not fail with a stack overflow`() {
        SchemaApi.withSchema(SchemaWithConcept::class) { schemaContext ->
            withRootInstance<SchemaWithConcept>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderMethodUsingSameBuilderDirectly::class,
                ) {
                    // do nothing
                }
            }
        }
    }
}
