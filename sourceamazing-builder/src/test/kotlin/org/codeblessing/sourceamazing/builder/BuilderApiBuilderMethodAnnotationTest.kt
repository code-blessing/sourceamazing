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
import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.validation.DataCollectorBuilderValidator
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.exceptions.MissingAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongFunctionSyntaxException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class BuilderApiBuilderMethodAnnotationTest {

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
    private interface BuilderMethodWithoutBuilderMethodAnnotation {

        @Suppress("UNUSED")
        fun doSomething()
    }

    @Test
    fun `test builder method without BuilderMethod annotation should throw an exception`() {
        assertThrows(WrongFunctionSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithoutBuilderMethodAnnotation::class) { dataCollector ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodReturningSameBuilder {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(): BuilderMethodReturningSameBuilder
    }

    @Test
    fun `test data collector with BuilderMethod annotation using the same builder should return without exception`() {
        // TODO is this valid without parameters?
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodReturningSameBuilder::class) { dataCollector ->
                // do nothing
            }
        }
    }


    @Builder
    private interface BuilderMethodReturningOtherBuildersWithoutBuilderAnnotation {


        private interface OtherBuilder

        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(builderClass = OtherBuilder::class)
        fun doSomething()
    }

    @Test
    fun `test sub-builder class without Builder annotation should throw an exception`() {
        assertThrows(MissingAnnotationSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodReturningOtherBuildersWithoutBuilderAnnotation::class) { dataCollector ->
                    // do nothing
                }
            }
        }
    }


    @Builder
    private interface BuilderMethodReturningOtherBuilderWithAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(builderClass = EmptyBuilder::class)
        fun doSomething()
    }

    @Test
    fun `test data collector sub-builder with annotation should return without exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodReturningOtherBuilderWithAnnotation::class) { dataCollector ->
                // do nothing
            }
        }
    }


    @Builder
    private interface BuilderMethodWithBuilderInjectionWithoutDeclaringNewBuilderAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: BuilderMethodWithBuilderInjectionWithoutDeclaringNewBuilderAnnotation.() -> Unit,
        )
    }

    @Test
    fun `test builder injection without declaring the builder should use the existing builder and return without exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderInjectionWithoutDeclaringNewBuilderAnnotation::class) { dataCollector ->
                // do nothing
            }
        }
    }


    @Builder
    private interface BuilderMethodHavingInjectedBuilderAsLastParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(builderClass = EmptyBuilder::class)
        fun doSomething(
            @InjectBuilder builder: EmptyBuilder.() -> Unit,
        )
    }

    @Test
    fun `test builder methods last parameter having the builder injection annotation should return without exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodHavingInjectedBuilderAsLastParameter::class) { dataCollector ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithTwoBuilderParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(builderClass = EmptyBuilder::class)
        fun doSomething(
            @InjectBuilder builder: EmptyBuilder.() -> Unit,
            @InjectBuilder anotherBuilder: EmptyBuilder.() -> Unit,
        )
    }

    @Test
    fun `test if more than one parameter have the builder injection annotation, an error is thrown`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithTwoBuilderParameter::class) { dataCollector ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithMissingFacetAnnotationOnParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(builderClass = EmptyBuilder::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptId: ConceptIdentifier,
            myValue: String,
            @InjectBuilder builder: EmptyBuilder.() -> Unit,
        )
    }

    @Test
    fun `test if one parameter is not having an annotation, an error is thrown`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithMissingFacetAnnotationOnParameter::class) { dataCollector ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithBuilderInjectionOnNonLastParam {
        interface MyFacetValue

        @Suppress("UNUSED")
        @BuilderMethod
        @WithNewBuilder(builderClass = EmptyBuilder::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptId: ConceptIdentifier,
            @InjectBuilder builder: EmptyBuilder.() -> Unit,
            @SetFacetValue(facetToModify = MyFacetValue::class) myValue: String,
        )
    }

    @Test
    fun `test builder inject annotation on a non-last parameter should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithBuilderInjectionOnNonLastParam::class) { dataCollector ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithSetConceptIdentifierAndIgnoreNullFacetValue {
        interface MyConceptClass

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(MyConceptClass::class)
        fun doSomething(
            @IgnoreNullFacetValue @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
        )
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and ConceptIdentifierValue annotation on same method should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodParamWithSetConceptIdentifierAndIgnoreNullFacetValue::class) { dataCollector ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodParamWithInjectBuilderAndIgnoreNullFacetValue {
        interface MyConceptClass

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(MyConceptClass::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @IgnoreNullFacetValue @InjectBuilder builder: EmptyBuilder.() -> Unit,
        )
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and InjectBuilder annotation on same method should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodParamWithInjectBuilderAndIgnoreNullFacetValue::class) { dataCollector ->
                    // do nothing
                }
            }
        }
    }


    @Builder
    private interface BuilderMethodWithIllegalConceptIdClass {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @SetConceptIdentifierValue conceptId: String,
        )
    }

    @Test
    fun `test concept id parameter without ConceptIdentifier class should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithIllegalConceptIdClass::class) { dataCollector ->
                    // do nothing
                }
            }
        }
    }
}