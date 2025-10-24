package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.BuilderApiAliasTest.MyClazzes.MyClazz
import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.api.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.UniqueId
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderApiAliasTest {
    private interface MyClazzes {

        enum class MyEnumeration {
            A,
            B,
            C,
        }

        interface MyClazz {
            val text: String
            val bool: Boolean
            val number: Int
            val enumeration: MyEnumeration
            val reference: MyClazz
        }

        val clazzes: List<MyClazz>
    }

    @Builder
    private interface BuilderMethodWithDuplicateAliasForNewClazz {

        @BuilderMethod
        @NewClazzModel(MyClazz::class, alias = "foo")
        @NewClazzModel(MyClazz::class, alias = "foo")
        fun doSomething()
    }

    @Test
    fun `test duplicate alias for NewClazzModel annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.ALIAS_IS_ALREADY_USED) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithDuplicateAliasForNewClazz::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndSubBuilderHavingDuplicatedAlias {

        @BuilderMethod
        @NewClazzModel(MyClazz::class, alias = "foo")
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodWithDuplicateAliasImportedFromSuperiorForNewClazz.() -> Unit
        )

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazz::class, alias = "foo")
        private interface BuilderMethodWithDuplicateAliasImportedFromSuperiorForNewClazz {

            @BuilderMethod @NewClazzModel(MyClazz::class, alias = "foo") fun doSomething()
        }
    }

    @Test
    fun `test duplicate alias from superior clazz for NewClazzModel annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.ALIAS_IS_ALREADY_USED) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithAliasAndSubBuilderHavingDuplicatedAlias::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithDuplicateAliasForManuallySetClazzModelId {

        @BuilderMethod
        @NewClazzModel(MyClazz::class, alias = "foo")
        fun doSomething(
            @SetAsClazzModelId(alias = "foo") clazzModelId1: UniqueId,
            @SetAsClazzModelId(alias = "foo") clazzModelId2: UniqueId,
        )
    }

    @Test
    fun `test duplicate alias for ClazzModelIdValue annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.DUPLICATE_SET_CLAZZ_IDENTIFIER_VALUE_USAGE
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithDuplicateAliasForManuallySetClazzModelId::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithoutClazzModelIdForAlias {

        @BuilderMethod @NewClazzModel(MyClazz::class, alias = "foo") fun doSomething()
    }

    @Test
    fun `test missing clazz identifier declaration for alias should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithoutClazzModelIdForAlias::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithoutAssignmentOfClazzModelId {

        @BuilderMethod @NewClazzModel(MyClazz::class, alias = "foo") fun doSomething(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazz::class, alias = "foo")
        private interface NestedBuilder {

            @BuilderMethod fun doSomethingNested(@SetAsClazzModelId(alias = "foo") id: UniqueId)
        }
    }

    @Test
    fun `test new clazz with assignment of clazz identifier but in the nested builder should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithoutAssignmentOfClazzModelId::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInClazzModelIdValueAnnotation {
        @BuilderMethod fun doSomething(@SetAsClazzModelId(alias = "unknown") id: UniqueId)
    }

    @Test
    fun `test use of unknown alias on SetClazzModelIdValue annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithUseOfUnknownAliasInClazzModelIdValueAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInClazzPropertyValueAnnotation {

        @BuilderMethod fun doSomething(@SetAsValue(alias = "unknown", clazzProperty = "text") value: String)
    }

    @Test
    fun `test use of unknown alias on SetClazzPropertyValue annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithUseOfUnknownAliasInClazzPropertyValueAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndSubBuilderHavingUnknownAlias {

        @BuilderMethod
        @NewClazzModel(MyClazz::class, alias = "known")
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodWithUseOfUnknownAliasInLinkClazzPropertyAnnotation.() -> Unit
        )

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazz::class, alias = "known")
        private interface BuilderMethodWithUseOfUnknownAliasInLinkClazzPropertyAnnotation {

            @BuilderMethod
            @SetClazzModelOfAlias(alias = "unknown", clazzProperty = "RefClazzProperty", referencedAlias = "known")
            fun doSomething()
        }
    }

    @Test
    fun `test use of unknown alias in property clazzToModifyAlias on the SetAliasClazzModelIdReferenceClazzPropertyValue annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithAliasAndSubBuilderHavingUnknownAlias::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndSubBuilderHavingUnknownReferenceAlias {

        @BuilderMethod
        @NewClazzModel(MyClazz::class, alias = "known")
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodWithUseOfUnknownReferenceAliasInLinkClazzPropertyAnnotation.() -> Unit
        )

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazz::class, alias = "known")
        private interface BuilderMethodWithUseOfUnknownReferenceAliasInLinkClazzPropertyAnnotation {

            @BuilderMethod
            @SetClazzModelOfAlias(alias = "known", clazzProperty = "RefClazzProperty", referencedAlias = "unknown")
            fun doSomething()
        }
    }

    @Test
    fun `test use of unknown alias in property referencedClazzAlias on SetAliasClazzModelIdReferenceClazzPropertyValue annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithAliasAndSubBuilderHavingUnknownReferenceAlias::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFixedStringClazzPropertyValueAnnotation {
        @BuilderMethod
        @SetFixedStringValue(alias = "unknown", clazzProperty = "text", value = "some text")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedStringClazzPropertyValue annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithUseOfUnknownAliasInFixedStringClazzPropertyValueAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFixedIntegerClazzPropertyValueAnnotation {
        @BuilderMethod
        @SetFixedIntValue(alias = "unknown", clazzProperty = "NumberClazzProperty", value = 42)
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedIntClazzPropertyValue annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithUseOfUnknownAliasInFixedIntegerClazzPropertyValueAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFixedEnumClazzPropertyValueAnnotation {
        @BuilderMethod
        @SetFixedEnumValue(alias = "unknown", clazzProperty = "EnumerationClazzProperty", value = "A")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedEnumClazzPropertyValue annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithUseOfUnknownAliasInFixedEnumClazzPropertyValueAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodCallingASubBuilderProvidingAnAlias {

        @BuilderMethod
        @NewClazzModel(MyClazz::class, alias = "known")
        @NewClazzModel(MyClazz::class, alias = "alsoKnown")
        fun doInjectASubBuilder(@InjectBuilder builder: BuilderMethodUsingAnAliasFromParentBuilder.() -> Unit)

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazz::class, alias = "known")
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazz::class, alias = "alsoKnown")
        private interface BuilderMethodUsingAnAliasFromParentBuilder {

            @BuilderMethod fun doSomething(@SetAsValue(alias = "known", clazzProperty = "text") value: String)
        }
    }

    @Test
    fun `test use of alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder annotation should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodCallingASubBuilderProvidingAnAlias::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodCallingASubBuilderProvidingAnAliasWithoutExpectAlias {

        @BuilderMethod
        @NewClazzModel(MyClazz::class, alias = "known")
        @NewClazzModel(MyClazz::class, alias = "alsoKnown")
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodUsingAnAliasFromParentBuilderWithoutExpectAlias.() -> Unit
        )

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazz::class, alias = "alsoKnown")
        private interface BuilderMethodUsingAnAliasFromParentBuilderWithoutExpectAlias {

            @BuilderMethod fun doSomething(@SetAsValue(alias = "known", clazzProperty = "text") valueForKnown: String)
        }
    }

    @Test
    fun `test omit alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodCallingASubBuilderProvidingAnAliasWithoutExpectAlias::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodCallingASubBuilderWithoutProvidingAnAlias {

        @BuilderMethod
        @NewClazzModel(MyClazz::class, alias = "known")
        @NewClazzModel(MyClazz::class, alias = "alsoKnown")
        fun doInjectASubBuilder(@InjectBuilder builder: BuilderMethodReusingAnAliasNameFromSuperiorBuilder.() -> Unit)

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazz::class, alias = "alsoKnown")
        private interface BuilderMethodReusingAnAliasNameFromSuperiorBuilder {

            @BuilderMethod
            @NewClazzModel(MyClazz::class, alias = "known")
            fun doSomething(@SetAsValue(alias = "known", clazzProperty = "text") valueForKnown: String)
        }
    }

    @Test
    fun `test reuse an alias that is not expected from superior builder should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodCallingASubBuilderWithoutProvidingAnAlias::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderWithDuplicateAliasesInExpectedAliasFromSuperiorBuilderAnnotation {

        @BuilderMethod @NewClazzModel(MyClazz::class, alias = "foo") fun doSomething(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazz::class, alias = "foo")
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazz::class, alias = "foo")
        private interface NestedBuilder {

            @BuilderMethod fun doSomethingNested()
        }
    }

    @Test
    fun `test duplicate alias in ExpectedAliasFromSuperiorBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.DUPLICATE_ALIAS_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderWithDuplicateAliasesInExpectedAliasFromSuperiorBuilderAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodCallingASubBuilderProvidingAnAliasWithRedeclaration {

        @BuilderMethod
        @NewClazzModel(MyClazz::class, alias = "hereKnown")
        @RedeclareAliasForNestedBuilder(alias = "hereKnown", newAlias = "thereKnown")
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodReusingARedeclaredAliasNameFromSuperiorBuilder.() -> Unit
        )

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazz::class, alias = "thereKnown")
        private interface BuilderMethodReusingARedeclaredAliasNameFromSuperiorBuilder {

            @BuilderMethod
            fun doSomething(@SetAsValue(alias = "thereKnown", clazzProperty = "text") valueForKnown: String)
        }
    }

    @Test
    fun `test redeclare an known alias should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(
                schemaContext,
                BuilderMethodCallingASubBuilderProvidingAnAliasWithRedeclaration::class,
            ) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderWithUnknownAliasesInRedeclareAliasForNestedBuilderAnnotation {

        @BuilderMethod
        @NewClazzModel(MyClazz::class, alias = "hereKnown")
        @RedeclareAliasForNestedBuilder(alias = "hereUnKnown", newAlias = "thereKnown")
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodReusingARedeclaredAliasNameFromSuperiorBuilder.() -> Unit
        )

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazz::class, alias = "thereKnown")
        private interface BuilderMethodReusingARedeclaredAliasNameFromSuperiorBuilder {

            @BuilderMethod
            fun doSomething(@SetAsValue(alias = "thereKnown", clazzProperty = "text") valueForKnown: String)
        }
    }

    @Test
    fun `test an unknown alias in ExpectedAliasFromSuperiorBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_REDECLARATION_ALIAS) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderWithUnknownAliasesInRedeclareAliasForNestedBuilderAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }
}
