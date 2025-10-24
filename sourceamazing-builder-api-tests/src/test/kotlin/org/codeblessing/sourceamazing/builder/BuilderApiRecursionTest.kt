package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderApiRecursionTest {
    private interface MyClazzes {

        interface OneClazz

        val clazzes: List<OneClazz>
    }

    @Builder
    private interface BuilderMethodUsingSameBuilderIndirectly {

        @BuilderMethod @NewClazzModel(MyClazzes.OneClazz::class, alias = "foo") fun doSomething(): OuterNestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.OneClazz::class, alias = "foo")
        private interface OuterNestedBuilder {

            @BuilderMethod fun doSomething(): InnerNestedBuilder
        }

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.OneClazz::class, alias = "foo")
        private interface InnerNestedBuilder {

            @BuilderMethod fun doSomething(): OuterNestedBuilder
        }
    }

    @Test
    fun `test using nested builder returning another inner nested builder that returns the first nested builder should not fail with a stack overflow`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodUsingSameBuilderIndirectly::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingSameBuilderDirectly {

        @BuilderMethod @NewClazzModel(MyClazzes.OneClazz::class, alias = "foo") fun doSomething(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.OneClazz::class, alias = "foo")
        private interface NestedBuilder {

            @BuilderMethod fun doSomething(): NestedBuilder
        }
    }

    @Test
    fun `test using nested builder returning itself should not fail with a stack overflow`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodUsingSameBuilderDirectly::class) {
                // do nothing
            }
        }
    }
}
