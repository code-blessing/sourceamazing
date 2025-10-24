package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.api.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderApiSchemaTest {
    private interface MyClazzes {

        interface MyKnownClazz {
            val knownClazzProperty: String
        }

        interface AlsoKnownClazz {
            val alsoKnownClazzProperty: String
        }

        interface UnknownClazz {
            val unknownClazzProperty: String
        }

        val knownClazzes: List<MyKnownClazz>

        val alsoKnownClazzes: List<AlsoKnownClazz>
    }

    @Builder
    private interface BuilderMethodCreatingKnownClazz {

        @BuilderMethod @NewClazzModel(MyClazzes.MyKnownClazz::class, alias = "foo") fun doSomething()
    }

    @Test
    fun `test using NewClazzModel annotation with known clazz should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodCreatingKnownClazz::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodCreatingUnknownClazz {

        @BuilderMethod @NewClazzModel(MyClazzes.UnknownClazz::class, alias = "foo") fun doSomething()
    }

    @Test
    fun `test using NewClazzModel annotation with unknown clazz should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_CLAZZ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodCreatingUnknownClazz::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingUnknownClazzPropertyAsParameterValue {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyKnownClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "UnknownClazzProperty") myValue: String)
    }

    @Test
    fun `test using unknown clazzProperty as parameter value of known clazz should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_CLAZZ_PROPERTY) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodUsingUnknownClazzPropertyAsParameterValue::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingUnknownClazzPropertyAsFixedValue {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyKnownClazz::class, alias = "foo")
        @SetFixedStringValue(alias = "foo", clazzProperty = "UnknownClazzProperty", value = "hello")
        fun doSomething()
    }

    @Test
    fun `test using unknown clazzProperty as fixed value of known clazz should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_CLAZZ_PROPERTY) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodUsingUnknownClazzPropertyAsFixedValue::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingClazzPropertyOfAnotherKnownClazzAsParameterValue {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyKnownClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "AlsoKnownClazzProperty") myValue: String)
    }

    @Test
    fun `test using known clazzProperty of another clazz as parameter value should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_CLAZZ_PROPERTY) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodUsingClazzPropertyOfAnotherKnownClazzAsParameterValue::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodUsingClazzPropertyOfAnotherKnownClazzAsFixedValue {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyKnownClazz::class, alias = "foo")
        @SetFixedStringValue(alias = "foo", clazzProperty = "AlsoKnownClazzProperty", value = "hello")
        fun doSomething()
    }

    @Test
    fun `test using known clazzProperty of another clazz as fixed value should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_CLAZZ_PROPERTY) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodUsingClazzPropertyOfAnotherKnownClazzAsFixedValue::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodPassingCorrectClazzParentNestedBuilderAsParameterValue {

        @BuilderMethod @NewClazzModel(MyClazzes.MyKnownClazz::class, alias = "foo") fun doSomething(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyKnownClazz::class, alias = "foo")
        private interface NestedBuilder {

            @BuilderMethod
            fun doSomethingNested(@SetAsValue(alias = "foo", clazzProperty = "knownClazzProperty") myValue: String)
        }
    }

    @Test
    fun `test using known clazzProperty of correct clazz as parameter value in nested builder should throw an exception`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(
                schemaContext,
                BuilderMethodPassingCorrectClazzParentNestedBuilderAsParameterValue::class,
            ) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodPassingCorrectClazzParentNestedBuilderAsFixedValue {

        @BuilderMethod @NewClazzModel(MyClazzes.MyKnownClazz::class, alias = "foo") fun doSomething(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyKnownClazz::class, alias = "foo")
        private interface NestedBuilder {

            @BuilderMethod
            @SetFixedStringValue(alias = "foo", clazzProperty = "knownClazzProperty", value = "hello")
            fun doSomethingNested()
        }
    }

    @Test
    fun `test using known clazzProperty of correct clazz as fixed value in nested builder should throw an exception`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(
                schemaContext,
                BuilderMethodPassingCorrectClazzParentNestedBuilderAsFixedValue::class,
            ) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodPassingWrongClazzParentNestedBuilderAsParameterValue {

        @BuilderMethod @NewClazzModel(MyClazzes.MyKnownClazz::class, alias = "foo") fun doSomething(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyKnownClazz::class, alias = "foo")
        private interface NestedBuilder {

            @BuilderMethod
            fun doSomethingWrongClazzProperty(
                @SetAsValue(alias = "foo", clazzProperty = "alsoKnownClazzProperty") myValue: String
            )

            @BuilderMethod
            fun doSomethingCorrectClazzProperty(
                @SetAsValue(alias = "foo", clazzProperty = "knownClazzProperty") myValue: String
            )
        }
    }

    @Test
    fun `test using known clazzProperty of wrong clazz as parameter value in nested builder should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_CLAZZ_PROPERTY) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodPassingWrongClazzParentNestedBuilderAsParameterValue::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodPassingWrongClazzParentNestedBuilderAsFixedValue {

        @BuilderMethod @NewClazzModel(MyClazzes.MyKnownClazz::class, alias = "foo") fun doSomething(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyKnownClazz::class, alias = "foo")
        private interface NestedBuilder {

            @BuilderMethod
            @SetFixedStringValue(alias = "foo", clazzProperty = "AlsoKnownClazzProperty", value = "hello")
            fun doSomethingWrongClazzProperty()

            @BuilderMethod
            @SetFixedStringValue(alias = "foo", clazzProperty = "KnownClazzProperty", value = "hello")
            fun doSomethingCorrectClazzProperty()
        }
    }

    @Test
    fun `test using known clazzProperty of wrong clazz as fixed value in nested builder should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_CLAZZ_PROPERTY) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodPassingWrongClazzParentNestedBuilderAsFixedValue::class,
                ) {
                    // do nothing
                }
            }
        }
    }
}
