package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.api.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.UniqueId
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderMethodApiMethodAnnotationsTest {

    private interface MyClazzes {
        enum class MyEnum {
            A,
            B,
        }

        interface MyClazz {
            val textClazzProperty: String

            val boolClazzProperty: Boolean

            val numberClazzProperty: Int

            val enumerationClazzProperty: MyEnum

            val selfRefClazzProperty: MyClazz
        }

        val clazzes: List<MyClazz>
    }

    @Builder
    private interface BuilderMethodWithFixedClazzPropertyValueAndParameterClazzPropertyValue {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        @SetFixedStringValue(alias = "foo", clazzProperty = "textClazzProperty", value = "fixed value")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "textClazzProperty") myText: String)
    }

    @Test
    fun `test string clazzProperty as fixed value and as parameter should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(
                schemaContext,
                BuilderMethodWithFixedClazzPropertyValueAndParameterClazzPropertyValue::class,
            ) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithMultipleFixedClazzPropertyValues {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        @SetFixedStringValue(alias = "foo", clazzProperty = "textClazzProperty", value = "fixed value 1")
        @SetFixedStringValue(alias = "foo", clazzProperty = "textClazzProperty", value = "fixed value 2")
        fun doSomething()
    }

    @Test
    fun `test string clazzProperty with multiple fixed values should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithMultipleFixedClazzPropertyValues::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCorrectFixedEnumClazzPropertyValues {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        @SetFixedEnumValue(alias = "foo", clazzProperty = "enumerationClazzProperty", value = "A")
        fun doSomething()
    }

    @Test
    fun `test enum clazzProperty with fixed values having correct enum should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithCorrectFixedEnumClazzPropertyValues::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongFixedEnumClazzPropertyValues {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        @SetFixedEnumValue(alias = "foo", clazzProperty = "enumerationClazzProperty", value = "NOT_A_NOR_B")
        fun doSomething()
    }

    @Test
    fun `test enum clazzProperty with fixed values having unknown enum value should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.WRONG_CLAZZ_PROPERTY_ENUM_VALUE) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongFixedEnumClazzPropertyValues::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongFixedClazzPropertyType {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        @SetFixedBooleanValue(alias = "foo", clazzProperty = "numberClazzProperty", value = true)
        fun doSomething()
    }

    @Test
    fun `test int clazzProperty with fixed boolean value should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.WRONG_CLAZZ_PROPERTY_TYPE) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongFixedClazzPropertyType::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUnregisteredFixedClazzPropertyType {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        @SetFixedIntValue(alias = "foo", clazzProperty = "unregisteredClazzProperty", value = 42)
        fun doSomething()
    }

    @Test
    fun `test int clazzProperty with fixed value for unregistered clazzProperty should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_CLAZZ_PROPERTY) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUnregisteredFixedClazzPropertyType::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithValidFixedReference {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "foo")
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "bar")
        @SetClazzModelOfAlias(alias = "foo", clazzProperty = "selfRefClazzProperty", referencedAlias = "bar")
        fun doSomething()
    }

    @Test
    fun `test referencing a clazz with fixed alias should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithValidFixedReference::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithValidParameterReference {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "foo")
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "bar")
        fun doSomething(@SetClazzModelOfId(alias = "foo", clazzProperty = "selfRefClazzProperty") myReference: UniqueId)
    }

    @Test
    fun `test referencing a clazz with parameter value should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithValidParameterReference::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithFunctionInsteadOfClazzModelIdReference {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "foo")
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "bar")
        fun doSomething(
            @SetClazzModelOfId(alias = "foo", clazzProperty = "selfRefClazzProperty") myReference: () -> UniqueId
        )
    }

    @Test
    fun `test reference clazzProperty with function instead of reference should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_REFERENCE_PARAMETER
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithFunctionInsteadOfClazzModelIdReference::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithStringInsteadOfClazzModelIdReference {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "foo")
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "bar")
        fun doSomething(@SetClazzModelOfId(alias = "foo", clazzProperty = "selfRefClazzProperty") myReference: String)
    }

    @Test
    fun `test reference clazzProperty with string instead of reference should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithStringInsteadOfClazzModelIdReference::class) {
                // do nothing
            }
        }
    }
}
