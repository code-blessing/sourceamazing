package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.withDefaultValueRootInstance
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
        fun doSomething(): OuterNestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts.OneConcept::class, conceptAlias = "foo")
        private interface OuterNestedBuilder {

            @BuilderMethod fun doSomething(): InnerNestedBuilder
        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts.OneConcept::class, conceptAlias = "foo")
        private interface InnerNestedBuilder {

            @BuilderMethod fun doSomething(): OuterNestedBuilder
        }
    }

    @Test
    fun `test using nested builder returning another inner nested builder that returns the first nested builder should not fail with a stack overflow`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            schemaContext.withDefaultValueRootInstance<MyConcepts> {
                BuilderApi.withBuilder(schemaContext, BuilderMethodUsingSameBuilderIndirectly::class) {
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
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts.OneConcept::class, conceptAlias = "foo")
        private interface NestedBuilder {

            @BuilderMethod fun doSomething(): NestedBuilder
        }
    }

    @Test
    fun `test using nested builder returning itself should not fail with a stack overflow`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            schemaContext.withDefaultValueRootInstance<MyConcepts> {
                BuilderApi.withBuilder(schemaContext, BuilderMethodUsingSameBuilderDirectly::class) {
                    // do nothing
                }
            }
        }
    }
}
