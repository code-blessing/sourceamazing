package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderApiRecursionTest {
    private interface MyConcepts {

        interface OneConcept

        val concepts: List<OneConcept>
    }

    @Builder
    private interface BuilderMethodUsingSameBuilderIndirectly {

        @BuilderMethod
        @NewConcept(MyConcepts.OneConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(OuterNestedBuilder::class)
        fun doSomething(): OuterNestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface OuterNestedBuilder {

            @BuilderMethod
            @WithNewBuilder(InnerNestedBuilder::class)
            fun doSomething(): InnerNestedBuilder
        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface InnerNestedBuilder {

            @BuilderMethod
            @WithNewBuilder(OuterNestedBuilder::class)
            fun doSomething(): OuterNestedBuilder
        }
    }

    @Test
    fun `test using nested builder returning another inner nested builder that returns the first nested builder should not fail with a stack overflow`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodUsingSameBuilderIndirectly::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingSameBuilderDirectly {

        @BuilderMethod
        @NewConcept(MyConcepts.OneConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(NestedBuilder::class)
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @BuilderMethod @WithNewBuilder(NestedBuilder::class) fun doSomething(): NestedBuilder
        }
    }

    @Test
    fun `test using nested builder returning itself should not fail with a stack overflow`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodUsingSameBuilderDirectly::class,
                ) {
                    // do nothing
                }
            }
        }
    }
}
