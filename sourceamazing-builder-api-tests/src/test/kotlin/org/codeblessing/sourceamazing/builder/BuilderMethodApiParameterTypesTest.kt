package org.codeblessing.sourceamazing.builder

import java.util.*
import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.api.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.UniqueId
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderMethodApiParameterTypesTest {
    enum class MyEnum {
        A,
        B,
        C,
    }

    enum class MyOtherIncompatibleEnum {
        A,
        B,
        C,
        D,
    }

    private interface MyClazzes {
        interface MyClazz {
            val textClazzProperty: String

            val otherTextClazzProperty: String

            val boolClazzProperty: Boolean

            val numberClazzProperty: Int

            val enumerationClazzProperty: MyEnum

            val selfRefClazzProperty: MyClazz
        }

        val clazzes: List<MyClazz>
    }

    @Builder
    private interface BuilderMethodWithNullableClazzModelId {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsClazzModelId(alias = "foo") clazzModelId: UniqueId?)
    }

    @Test
    fun `test clazz id parameter as nullable type should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_CLAZZ_IDENTIFIER_TYPE_NO_NULLABLE
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithNullableClazzModelId::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableSetClazzModelIdAndIgnoreNullClazzPropertyValueAnnotation {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@IgnoreNullValue @SetAsClazzModelId(alias = "foo") clazzModelId: UniqueId?)
    }

    @Test
    fun `test clazz id parameter as nullable type with IgnoreNullClazzPropertyValue annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_CLAZZ_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithNullableSetClazzModelIdAndIgnoreNullClazzPropertyValueAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithSetClazzModelIdAndIgnoreNullClazzPropertyValueAnnotation {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@IgnoreNullValue @SetAsClazzModelId(alias = "foo") clazzModelId: UniqueId)
    }

    @Test
    fun `test IgnoreNullClazzPropertyValue annotation and ClazzModelIdValue annotation on same method should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_CLAZZ_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodParamWithSetClazzModelIdAndIgnoreNullClazzPropertyValueAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableParameterWithoutIgnoreNullClazzPropertyValueAnnotation {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "textClazzProperty") myText: String?)
    }

    @Test
    fun `test string clazzProperty parameter as nullable type without IgnoreNullClazzPropertyValue should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithNullableParameterWithoutIgnoreNullClazzPropertyValueAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableParameterWithIgnoreNullClazzPropertyValueAnnotation {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(
            @IgnoreNullValue @SetAsValue(alias = "foo", clazzProperty = "textClazzProperty") myText: String?
        )
    }

    @Test
    fun `test string clazzProperty parameter as nullable type with IgnoreNullClazzPropertyValue should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(
                schemaContext,
                BuilderMethodWithNullableParameterWithIgnoreNullClazzPropertyValueAnnotation::class,
            ) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCollectionTypedStringParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "textClazzProperty") myTexts: List<String>)
    }

    @Test
    fun `test string clazzProperty parameter with collection type instead of string should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithCollectionTypedStringParameter::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithMultipleSetClazzPropertyValueOnSameClazzPropertyMethod {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(
            @SetAsValue(alias = "foo", clazzProperty = "textClazzProperty") myText1: String,
            @SetAsValue(alias = "foo", clazzProperty = "textClazzProperty") myText2: String,
        )
    }

    @Test
    fun `test multiple assignments via parameter value for the same clazzProperty should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(
                schemaContext,
                BuilderMethodWithMultipleSetClazzPropertyValueOnSameClazzPropertyMethod::class,
            ) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithSortedSetStringCollectionParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "textClazzProperty") myTexts: SortedSet<String>)
    }

    @Test
    fun `test string clazzProperty parameter with SortedSet collection of string should throw an exception as only List, Set and Array are supported`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER,
            BuilderErrorCode.BUILDER_PARAM_WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithSortedSetStringCollectionParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCollectionTypedNullableStringParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "textClazzProperty") myTexts: List<String?>)
    }

    @Test
    fun `test string clazzProperty parameter with collection type of nullable string should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithCollectionTypedNullableStringParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCollectionTypedNullableStringParameterWithIgnoreNullClazzPropertyValueAnnotation {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(
            @IgnoreNullValue @SetAsValue(alias = "foo", clazzProperty = "textClazzProperty") myTexts: List<String?>
        )
    }

    @Test
    fun `test string clazzProperty parameter with collection type of nullable string and IgnoreNullClazzPropertyValue annotation should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(
                schemaContext,
                BuilderMethodWithCollectionTypedNullableStringParameterWithIgnoreNullClazzPropertyValueAnnotation::class,
            ) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedStringParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "textClazzProperty") myText: Int)
    }

    @Test
    fun `test string clazzProperty parameter with other type than string should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER,
            BuilderErrorCode.BUILDER_PARAM_WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongTypedStringParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongFunctionReturningStringParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "textClazzProperty") myText: () -> String)
    }

    @Test
    fun `test string clazzProperty parameter with a function returning a string should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongFunctionReturningStringParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedBooleanParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "boolClazzProperty") myBoolean: Int)
    }

    @Test
    fun `test boolean clazzProperty parameter with other type than boolean should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER,
            BuilderErrorCode.BUILDER_PARAM_WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongTypedBooleanParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithArrayOfBooleanParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "boolClazzProperty") myBooleans: Array<Boolean>)
    }

    @Test
    fun `test boolean clazzProperty parameter with array of boolean instead of boolean should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithArrayOfBooleanParameter::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedIntParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "numberClazzProperty") myInt: String)
    }

    @Test
    fun `test int clazzProperty parameter with other type than int should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER,
            BuilderErrorCode.BUILDER_PARAM_WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongTypedIntParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithLongInsteadOfIntParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "numberClazzProperty") myInt: Long)
    }

    @Test
    fun `test int clazzProperty parameter with long type should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER,
            BuilderErrorCode.BUILDER_PARAM_WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithLongInsteadOfIntParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNumberParameterInsteadOfIntParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "numberClazzProperty") myInt: Number)
    }

    @Test
    fun `test int clazzProperty parameter with Number type should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER,
            BuilderErrorCode.BUILDER_PARAM_WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithNumberParameterInsteadOfIntParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAnyParameterInsteadOfIntParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "numberClazzProperty") myInt: Any)
    }

    @Test
    fun `test int clazzProperty parameter with Any type should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER,
            BuilderErrorCode.BUILDER_PARAM_WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithAnyParameterInsteadOfIntParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithIntInsteadOfEnumParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "enumerationClazzProperty") myEnum: Int)
    }

    @Test
    fun `test enum clazzProperty parameter with wrong int type instead of enum should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER,
            BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithIntInsteadOfEnumParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithOtherIncompatibleEnumParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(
            @SetAsValue(alias = "foo", clazzProperty = "enumerationClazzProperty") myEnum: MyOtherIncompatibleEnum
        )
    }

    @Test
    fun `test enum clazzProperty parameter with other incompatible enum instead of correct enum should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER,
            BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithOtherIncompatibleEnumParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithStringInsteadOfEnumParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "enumerationClazzProperty") myEnum: String)
    }

    @Test
    fun `test enum clazzProperty parameter with string instead of enum should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER,
            BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithStringInsteadOfEnumParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCorrectEnumParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "enumerationClazzProperty") myEnum: MyEnum)
    }

    @Test
    fun `test enum clazzProperty parameter should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithCorrectEnumParameter::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithSetOfEnumParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "enumerationClazzProperty") myEnums: Set<MyEnum>)
    }

    @Test
    fun `test enum clazzProperty parameter with set of enum instead of single enum should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithSetOfEnumParameter::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableSetOfEnumParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "enumerationClazzProperty") myEnums: Set<MyEnum>?)
    }

    @Test
    fun `test enum clazzProperty parameter with nullable set of enum instead of single enum should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_NO_NULLABLE_COLLECTION_TYPE
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithNullableSetOfEnumParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedReferenceParameter {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetAsValue(alias = "foo", clazzProperty = "selfRefClazzProperty") myRef: String)
    }

    @Test
    fun `test reference clazzProperty parameter with other type than a ClazzModelId should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER,
            BuilderErrorCode.BUILDER_PARAM_WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongTypedReferenceParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithVarargArray {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(
            @SetClazzModelOfId(alias = "foo", clazzProperty = "selfRefClazzProperty") vararg myRefs: UniqueId
        )
    }

    @Test
    fun `test reference clazzProperty parameter with vararg array of references should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithVarargArray::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithClazzModelId {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo")
        fun doSomething(@SetClazzModelOfId(alias = "foo", clazzProperty = "selfRefClazzProperty") myRef: UniqueId)
    }

    @Test
    fun `test reference clazzProperty parameter with correct reference type should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithClazzModelId::class) {
                // do nothing
            }
        }
    }
}
