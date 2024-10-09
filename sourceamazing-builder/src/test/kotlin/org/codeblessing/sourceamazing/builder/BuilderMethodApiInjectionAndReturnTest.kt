package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.exceptions.MissingAnnotationSyntaxException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class BuilderMethodApiInjectionAndReturnTest {

    @Schema(concepts = [SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class])
    private interface SchemaWithConceptWithTextFacet {
        @Concept(facets = [ConceptWithTextFacet.TextFacet::class])
        interface ConceptWithTextFacet {
            @StringFacet
            interface TextFacet
        }
    }

    @Builder
    private interface EmptyBuilder


    @Builder
    private interface BuilderMethodReturningSameBuilder {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(): BuilderMethodReturningSameBuilder
    }

    @Test
    fun `test builder with BuilderMethod annotation using the same builder should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodReturningSameBuilder::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodReturningOtherBuilder {

        @Builder
        private interface OtherBuilder

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(): OtherBuilder
    }

    @Test
    fun `test builder method returning another builder should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodReturningOtherBuilder::class) { builder ->
                // do nothing
            }
        }
    }


    @Builder
    private interface BuilderMethodReturningOtherBuildersWithoutBuilderAnnotation {

        private interface OtherBuilder

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(): OtherBuilder
    }

    @Test
    fun `test builder method returning another builder without Builder annotation should throw an exception`() {
        assertThrows(MissingAnnotationSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodReturningOtherBuildersWithoutBuilderAnnotation::class) { builder ->
                    // do nothing
                }
            }
        }
    }


    @Builder
    private interface BuilderMethodNoReturnTypeButWithNewBuilderAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(builderClass = EmptyBuilder::class)
        fun doSomething()
    }

    @Test
    fun `test builder method returning no builder but declaring a builder with WithNewBuilder annotation should throw an exception`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodNoReturnTypeButWithNewBuilderAnnotation::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodReturningOtherBuilderThanDeclaredInWithNewBuilderAnnotation {

        @Builder
        private interface OtherBuilder


        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(builderClass = OtherBuilder::class)
        fun doSomething(): EmptyBuilder
    }

    @Test
    fun `test builder method having different return type builder and WithNewBuilder annotation declared should throw an exception`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodReturningOtherBuilderThanDeclaredInWithNewBuilderAnnotation::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodReturningOtherBuilderWithNewBuilderAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(builderClass = EmptyBuilder::class)
        fun doSomething(): EmptyBuilder
    }

    @Test
    fun `test builder method returning a another with WithNewBuilder annotation should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodReturningOtherBuilderWithNewBuilderAnnotation::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionWithoutDeclaringWithNewBuilderAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: BuilderMethodWithBuilderInjectionWithoutDeclaringWithNewBuilderAnnotation.() -> Unit,
        )
    }

    @Test
    fun `test builder injection without declaring the builder should use the existing builder and not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderInjectionWithoutDeclaringWithNewBuilderAnnotation::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionWithoutExtensionFunction {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: () -> Unit,
        )
    }

    @Test
    fun `test builder injection not having the extension function parameter should throw an exception`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderInjectionWithoutExtensionFunction::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionWithValueParameters {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: EmptyBuilder.(EmptyBuilder) -> Unit,
        )
    }

    @Test
    fun `test builder injection having value parameters should throw an exception`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderInjectionWithValueParameters::class) { builder ->
                    // do nothing
                }
            }
        }
    }


    @Builder
    private interface BuilderMethodWithBuilderInjectionWithReturnType {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: EmptyBuilder.() -> EmptyBuilder,
        )
    }

    @Test
    fun `test builder injection returning a builder should throw an exception`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderInjectionWithReturnType::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
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
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithTwoBuilderInjectionParameter::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionOnNonLastParam {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @SetConceptIdentifierValue conceptId: ConceptIdentifier,
            @InjectBuilder builder: EmptyBuilder.() -> Unit,
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.TextFacet::class) myValue: String,
        )
    }

    @Test
    fun `test builder inject annotation on a non-last parameter should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderInjectionOnNonLastParam::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithInjectBuilderAndIgnoreNullFacetValue {
        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @IgnoreNullFacetValue @InjectBuilder builder: EmptyBuilder.() -> Unit,
        )
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and InjectBuilder annotation on same method should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodParamWithInjectBuilderAndIgnoreNullFacetValue::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionAndReturnTypeBuilder {
        @Builder
        private interface OtherBuilder

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: EmptyBuilder.() -> Unit,
        ): EmptyBuilder
    }

    @Test
    fun `test builder inject and returned builder should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderInjectionAndReturnTypeBuilder::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
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
    fun `test builder inject with another builder in WithNewBuilder annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderInjectionAndWithNewBuilderAnnotationDifferentTypes::class) { builder ->
                    // do nothing
                }
            }
        }
    }

}