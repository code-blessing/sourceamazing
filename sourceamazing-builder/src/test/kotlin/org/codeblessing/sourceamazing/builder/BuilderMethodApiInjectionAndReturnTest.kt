package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.exceptions.MissingClassAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.codeblessing.sourceamazing.toConceptName
import org.junit.jupiter.api.Test

class BuilderMethodApiInjectionAndReturnTest {

    private interface SchemaWithConceptWithTextFacet {
        interface ConceptWithTextFacet {
            @Suppress("UNUSED")
            @Facet
            val text: String
        }

        @Suppress("UNUSED")
        @Facet
        val concepts: List<ConceptWithTextFacet>
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface EmptyBuilder


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodReturningSameBuilder {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(): BuilderMethodReturningSameBuilder
    }

    @Test
    fun `test builder with BuilderMethod annotation using the same builder should fail`() {
        assertExceptionWithErrorCode(
            WrongAnnotationSyntaxException::class,
            SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodReturningSameBuilder::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodReturningOtherBuilder {

        @Builder
        private interface OtherBuilder

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(): OtherBuilder
    }

    @Test
    fun `test builder method returning another builder should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderMethodReturningOtherBuilder::class,
                ) {
                    // do nothing
                }
            }
        }
    }


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodReturningOtherBuildersWithoutBuilderAnnotation {

        private interface OtherBuilder

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(): OtherBuilder
    }

    @Test
    fun `test builder method returning another builder without Builder annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            MissingClassAnnotationSyntaxException::class,
            SchemaErrorCode.MUST_HAVE_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodReturningOtherBuildersWithoutBuilderAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodNoReturnTypeButWithNewBuilderAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(builderClass = EmptyBuilder::class)
        fun doSomething()
    }

    @Test
    fun `test builder method returning no builder but declaring a builder with WithNewBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_DECLARED_IN_WITH_NEW_BUILDER_ANNOTATION_MUST_BE_USED,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodNoReturnTypeButWithNewBuilderAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodReturningOtherBuilderThanDeclaredInWithNewBuilderAnnotation {
        private interface OtherBuilder


        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(builderClass = OtherBuilder::class)
        fun doSomething(): EmptyBuilder
    }

    @Test
    fun `test builder method having different return type builder and WithNewBuilder annotation declared should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_IN_WITH_NEW_BUILDER_MUST_BE_SAME,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodReturningOtherBuilderThanDeclaredInWithNewBuilderAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodReturningOtherBuilderWithNewBuilderAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(builderClass = AnotherSubBuilder::class)
        fun doSomething(): AnotherSubBuilder

        @Builder
        private interface AnotherSubBuilder

    }

    @Test
    fun `test builder method returning a another with WithNewBuilder annotation should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderMethodReturningOtherBuilderWithNewBuilderAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithBuilderInjectionWithoutDeclaringWithNewBuilderAnnotation {


        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(builderClass = AnotherSubBuilder::class)
        fun doSomething(): AnotherSubBuilder

        @Builder
        private interface AnotherSubBuilder {
            @Suppress("UNUSED")
            @BuilderMethod
            fun doSomething(
                @InjectBuilder builder: AnotherSubBuilder.() -> Unit,
            )

        }
    }

    @Test
    fun `test builder injection without declaring the builder should use the existing builder and not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderMethodWithBuilderInjectionWithoutDeclaringWithNewBuilderAnnotation::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithBuilderInjectionWithoutExtensionFunction {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: () -> Unit,
        )
    }

    @Test
    fun `test builder injection not having the extension function parameter should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_INJECTION_PARAMS_INVALID,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithBuilderInjectionWithoutExtensionFunction::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithBuilderInjectionWithObjectInsteadOfFunction {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: Any,
        )
    }

    @Test
    fun `test builder injection not passing a function function parameter should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_INJECTION_PARAMS_INVALID,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithBuilderInjectionWithObjectInsteadOfFunction::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithBuilderInjectionWithValueParameters {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: EmptyBuilder.(EmptyBuilder) -> Unit,
        )
    }

    @Test
    fun `test builder injection having value parameters should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_INJECTION_PARAMS_INVALID,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithBuilderInjectionWithValueParameters::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithBuilderInjectionWithReturnType {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: EmptyBuilder.() -> EmptyBuilder,
        )
    }

    @Test
    fun `test builder injection returning a builder should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_HAVE_RETURN_TYPE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithBuilderInjectionWithReturnType::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithBuilderInjectionWithNullableType {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: (EmptyBuilder.() -> Unit)?,
        )
    }

    @Test
    fun `test builder injection with a nullable type should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_BE_NULLABLE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithBuilderInjectionWithNullableType::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithTwoBuilderInjectionParameter {

        @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithTwoBuilderInjectionParameter::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithBuilderInjectionOnNonLastParam {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
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
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithBuilderInjectionOnNonLastParam::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodParamWithInjectBuilderAndIgnoreNullFacetValue {
        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
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
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodParamWithInjectBuilderAndIgnoreNullFacetValue::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithBuilderInjectionAndReturnTypeBuilder {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: EmptyBuilder.() -> Unit,
        ): EmptyBuilder
    }

    @Test
    fun `test builder inject and returned builder should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_INJECTION_AND_RETURN_AT_SAME_TIME,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithBuilderInjectionAndReturnTypeBuilder::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithBuilderInjectionAndWithNewBuilderAnnotationDifferentTypes {
        @Builder
        private interface OtherBuilder

        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(OtherBuilder::class)
        fun doSomething(
            @InjectBuilder builder: EmptyBuilder.() -> Unit,
        )
    }

    @Test
    fun `test builder inject with another builder in WithNewBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.BUILDER_IN_WITH_NEW_BUILDER_MUST_BE_SAME,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithTextFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithTextFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithBuilderInjectionAndWithNewBuilderAnnotationDifferentTypes::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }
}
