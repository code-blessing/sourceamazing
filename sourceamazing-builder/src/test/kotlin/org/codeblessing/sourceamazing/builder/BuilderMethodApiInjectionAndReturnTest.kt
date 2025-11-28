package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderMethodApiInjectionAndReturnTest {

    private interface MyConcepts {
        interface MyConcept {
            val text: String
        }

        val concepts: List<MyConcept>
    }

    @Builder private interface EmptyBuilder

    @Builder
    private interface BuilderMethodReturningSameBuilder {

        @BuilderMethod fun doSomething(): BuilderMethodReturningSameBuilder
    }

    @Test
    fun `test builder with BuilderMethod annotation using the same builder should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderMethodReturningSameBuilder::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodReturningOtherBuilder {

        @Builder private interface OtherBuilder

        @BuilderMethod fun doSomething(): OtherBuilder
    }

    @Test
    fun `test builder method returning another builder should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderMethodReturningOtherBuilder::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodReturningOtherBuildersWithoutBuilderAnnotation {

        private interface OtherBuilder

        @BuilderMethod fun doSomething(): OtherBuilder
    }

    @Test
    fun `test builder method returning another builder without Builder annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderSyntaxException::class, BuilderErrorCode.MUST_HAVE_ANNOTATION) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodReturningOtherBuildersWithoutBuilderAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodNoReturnTypeButWithNewBuilderAnnotation {

        @BuilderMethod @WithNewBuilder(builderClass = EmptyBuilder::class) fun doSomething()
    }

    @Test
    fun `test builder method returning no builder but declaring a builder with WithNewBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_DECLARED_IN_WITH_NEW_BUILDER_ANNOTATION_MUST_BE_USED,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, BuilderMethodNoReturnTypeButWithNewBuilderAnnotation::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodReturningOtherBuilderThanDeclaredInWithNewBuilderAnnotation {
        private interface OtherBuilder

        @BuilderMethod @WithNewBuilder(builderClass = OtherBuilder::class) fun doSomething(): EmptyBuilder
    }

    @Test
    fun `test builder method having different return type builder and WithNewBuilder annotation declared should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_IN_WITH_NEW_BUILDER_MUST_BE_SAME,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodReturningOtherBuilderThanDeclaredInWithNewBuilderAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodReturningOtherBuilderWithNewBuilderAnnotation {

        @BuilderMethod @WithNewBuilder(builderClass = AnotherSubBuilder::class) fun doSomething(): AnotherSubBuilder

        @Builder private interface AnotherSubBuilder
    }

    @Test
    fun `test builder method returning a another with WithNewBuilder annotation should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodReturningOtherBuilderWithNewBuilderAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionWithoutDeclaringWithNewBuilderAnnotation {

        @BuilderMethod @WithNewBuilder(builderClass = AnotherSubBuilder::class) fun doSomething(): AnotherSubBuilder

        @Builder
        private interface AnotherSubBuilder {
            @BuilderMethod fun doSomething(@InjectBuilder builder: AnotherSubBuilder.() -> Unit)
        }
    }

    @Test
    fun `test builder injection without declaring the builder should use the existing builder and not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithBuilderInjectionWithoutDeclaringWithNewBuilderAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionWithoutExtensionFunction {

        @BuilderMethod fun doSomething(@InjectBuilder builder: () -> Unit)
    }

    @Test
    fun `test builder injection not having the extension function parameter should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_INJECTION_PARAMS_INVALID,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithBuilderInjectionWithoutExtensionFunction::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionWithObjectInsteadOfFunction {

        @BuilderMethod fun doSomething(@InjectBuilder builder: Any)
    }

    @Test
    fun `test builder injection not passing a function function parameter should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_INJECTION_PARAMS_INVALID,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithBuilderInjectionWithObjectInsteadOfFunction::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionWithValueParameters {

        @BuilderMethod fun doSomething(@InjectBuilder builder: EmptyBuilder.(EmptyBuilder) -> Unit)
    }

    @Test
    fun `test builder injection having value parameters should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_INJECTION_PARAMS_INVALID,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderInjectionWithValueParameters::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionWithReturnType {

        @BuilderMethod fun doSomething(@InjectBuilder builder: EmptyBuilder.() -> EmptyBuilder)
    }

    @Test
    fun `test builder injection returning a builder should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_HAVE_RETURN_TYPE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderInjectionWithReturnType::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionWithNullableType {

        @BuilderMethod fun doSomething(@InjectBuilder builder: (EmptyBuilder.() -> Unit)?)
    }

    @Test
    fun `test builder injection with a nullable type should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_BE_NULLABLE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderInjectionWithNullableType::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithTwoBuilderInjectionParameter {

        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: EmptyBuilder.() -> Unit,
            @InjectBuilder anotherBuilder: EmptyBuilder.() -> Unit,
        )
    }

    @Test
    fun `test builder method with more than one injected builder should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_ONLY_LAST_PARAM_CAN_BE_INJECTION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, BuilderMethodWithTwoBuilderInjectionParameter::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionOnNonLastParam {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptId: ConceptIdentifier,
            @InjectBuilder builder: EmptyBuilder.() -> Unit,
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "text") myValue: String,
        )
    }

    @Test
    fun `test builder inject annotation on a non-last parameter should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_ONLY_LAST_PARAM_CAN_BE_INJECTION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderInjectionOnNonLastParam::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithInjectBuilderAndIgnoreNullFacetValue {
        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier: ConceptIdentifier,
            @IgnoreNullFacetValue @InjectBuilder builder: EmptyBuilder.() -> Unit,
        )
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and InjectBuilder annotation on same method should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_INJECTION_AND_IGNORE_NULL_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodParamWithInjectBuilderAndIgnoreNullFacetValue::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionAndReturnTypeBuilder {
        @BuilderMethod fun doSomething(@InjectBuilder builder: EmptyBuilder.() -> Unit): EmptyBuilder
    }

    @Test
    fun `test builder inject and returned builder should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_INJECTION_AND_RETURN_AT_SAME_TIME,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithBuilderInjectionAndReturnTypeBuilder::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionAndWithNewBuilderAnnotationDifferentTypes {
        @Builder private interface OtherBuilder

        @BuilderMethod
        @WithNewBuilder(OtherBuilder::class)
        fun doSomething(@InjectBuilder builder: EmptyBuilder.() -> Unit)
    }

    @Test
    fun `test builder inject with another builder in WithNewBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_IN_WITH_NEW_BUILDER_MUST_BE_SAME,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithBuilderInjectionAndWithNewBuilderAnnotationDifferentTypes::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }
}
