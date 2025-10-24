package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.api.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderApiClazzPropertyMultiUseTest {
    private interface MyClasses {

        interface ClazzWithClazzPropertyAlphaAndBeta {
            val clazzPropertyAlpha: String

            val clazzPropertyBeta: String
        }

        interface ClazzWithClazzPropertyAlpha {
            val clazzPropertyAlpha: String
        }

        interface ClazzWithClazzPropertyBeta {
            val clazzPropertyBeta: String
        }

        val clazzAlphaAndBeta: List<ClazzWithClazzPropertyAlphaAndBeta>

        val clazzAlpha: List<ClazzWithClazzPropertyAlpha>

        val clazzBeta: List<ClazzWithClazzPropertyBeta>
    }

    @Builder
    private interface BuilderMethodUsingSameBuilderForDifferentClazzesWithOverlappingClazzProperties {

        @BuilderMethod
        @NewClazzModel(MyClasses.ClazzWithClazzPropertyAlphaAndBeta::class, alias = "foo")
        fun doSomethingClazzWithClazzPropertyAlphaAndBeta(): NestedBuilder

        @BuilderMethod
        @NewClazzModel(MyClasses.ClazzWithClazzPropertyAlpha::class, alias = "foo")
        fun doSomethingWithClazzWithClazzPropertyAlpha(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(
            clazz = MyClasses.ClazzWithClazzPropertyAlphaAndBeta::class,
            alias = "foo",
        )
        private interface NestedBuilder {

            @BuilderMethod
            fun doSomethingWithClazzPropertyAlpha(
                @SetAsValue(alias = "foo", clazzProperty = "clazzPropertyAlpha") myValue: String
            )
        }
    }

    @Test
    fun `test using nested builder for two different clazzes even with overlapping clazzProperties should fail`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.EXPECTED_CLAZZ_FROM_SUPERIOR_BUILDER_ANNOTATION_NOT_MATCHING_PROVIDED_CLAZZ
        ) {
            SchemaApi.withSchema(MyClasses::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodUsingSameBuilderForDifferentClazzesWithOverlappingClazzProperties::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingSameBuilderForDifferentClazzesWithoutOverlappingClazzProperties {

        @BuilderMethod
        @NewClazzModel(MyClasses.ClazzWithClazzPropertyAlphaAndBeta::class, alias = "foo")
        fun doSomethingClazzWithClazzPropertyAlphaAndBeta(): NestedBuilder

        @BuilderMethod
        @NewClazzModel(MyClasses.ClazzWithClazzPropertyBeta::class, alias = "foo")
        fun doSomethingWithClazzWithClazzPropertyBeta(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(
            clazz = MyClasses.ClazzWithClazzPropertyAlphaAndBeta::class,
            alias = "foo",
        )
        private interface NestedBuilder {

            @BuilderMethod
            fun doSomethingWithClazzPropertyAlpha(
                @SetAsValue(alias = "foo", clazzProperty = "clazzPropertyAlpha") myValue: String
            )
        }
    }

    @Test
    fun `test using nested builder for two different clazzes without overlapping clazzProperties should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.EXPECTED_CLAZZ_FROM_SUPERIOR_BUILDER_ANNOTATION_NOT_MATCHING_PROVIDED_CLAZZ
        ) {
            SchemaApi.withSchema(MyClasses::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodUsingSameBuilderForDifferentClazzesWithoutOverlappingClazzProperties::class,
                ) {
                    // do nothing
                }
            }
        }
    }
}
