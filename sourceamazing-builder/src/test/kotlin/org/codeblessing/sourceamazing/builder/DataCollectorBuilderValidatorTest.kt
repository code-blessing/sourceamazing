package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderException
import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.typemirror.IgnoreNullFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.InjectBuilderAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.NewConceptAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetConceptIdentifierValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.WithNewBuilderAnnotationMirror
import org.codeblessing.sourceamazing.schema.schemacreator.CommonMirrors
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaMirrorDsl
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirror
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DataCollectorBuilderValidatorTest {

    @Builder
    private interface EmptyBuilder

    @Test
    fun `test data collector without annotation should throw an exception`() {
        val builder = BuilderMirrorDsl.builder(addBuilderAnnotation = false) {
            // empty builder without annotation
        }

        assertThrows(DataCollectorBuilderException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test builder class not being an interface should throw an exception`() {
        val builder = BuilderMirrorDsl.builder {
            setBuilderIsClass()
        }
        assertThrows(DataCollectorBuilderException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }


    @Test
    fun `test data collector without accessor method should return without exception`() {
        val builder = BuilderMirrorDsl.builder {
            // builder without methods
        }

        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
    }

    @Test
    fun `test builder method without BuilderMethod annotation should throw an exception`() {
        val builder = BuilderMirrorDsl.builder {
            builderMethod(addBuilderMethodAnnotation = false) {
                // no parameter and return type
            }
        }
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test data collector with BuilderMethod annotation using the same builder should return without exception`() {
        val builder = BuilderMirrorDsl.builder {
            builderMethod {
                // no parameter and return type
            }
        }
        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
    }


    @Builder
    private interface DataCollectorWithOtherBuildersWithoutAnnotation {


        private interface OtherBuilder

        @BuilderMethod
        @WithNewBuilder(builderClass = OtherBuilder::class)
        fun doSomething()
    }

    @Test
    fun `test sub-builder class without Builder annotation should throw an exception`() {
        val otherBuilder = BuilderMirrorDsl.builder(addBuilderAnnotation = false) {
            // empty builder without annotation
        }
        val builder = BuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(WithNewBuilderAnnotationMirror(builderClass = otherBuilder))
                // no parameter and return type
            }
        }

        assertThrows(DataCollectorBuilderException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }


    @Test
    fun `test data collector sub-builder with annotation should return without exception`() {
        val emptyBuilder = BuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = BuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(WithNewBuilderAnnotationMirror(builderClass = emptyBuilder))
                // no parameter and return type
            }
        }

        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
    }


    @Test
    fun `test builder method without base BuilderMethod annotation should throw an exception`() {
        val emptyBuilder = BuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = BuilderMirrorDsl.builder {
            builderMethod(addBuilderMethodAnnotation = false) {
                withAnnotationOnMethod(WithNewBuilderAnnotationMirror(builderClass = emptyBuilder))
                // no parameter and return type
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test builder injection without declaring the builder should use the existing builder and return without exception`() {
        val builder = BuilderMirrorDsl.builder {
            builderMethod {
                withFunctionParameter(
                    parameterName = "builder",
                    parameterFunction = FunctionMirror
                        .anonymousFunctionMirror()
                        .withNoReturnType(),
                        // .withReceiverType(this@builder), to assign Builder interface as receiver type is not possible
                    nullable = false,
                    InjectBuilderAnnotationMirror(),
                )
            }
        }


        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
    }


    @Test
    fun `test builder methods last parameter having the builder injection annotation should return without exception`() {
        val emptyBuilder = BuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = BuilderMirrorDsl.builder {
            withAnnotationOnBuilder(WithNewBuilderAnnotationMirror(builderClass = emptyBuilder))
            builderMethod {
                withFunctionParameter(
                    parameterName = "builder",
                    parameterFunction = FunctionMirror
                        .anonymousFunctionMirror()
                        .withReceiverType(emptyBuilder)
                        .withNoReturnType(),
                    nullable = false,
                    InjectBuilderAnnotationMirror(),
                )
            }
        }


        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
    }

    @Test
    fun `test if more than one parameter have the builder injection annotation, an error is thrown`() {
        val emptyBuilder = BuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = BuilderMirrorDsl.builder {
            withAnnotationOnBuilder(WithNewBuilderAnnotationMirror(builderClass = emptyBuilder))
            builderMethod {
                withFunctionParameter(
                    parameterName = "builder",
                    parameterFunction = FunctionMirror
                        .anonymousFunctionMirror()
                        .withReceiverType(emptyBuilder)
                        .withNoReturnType(),
                    nullable = false,
                    InjectBuilderAnnotationMirror(),
                )
                withFunctionParameter(
                    parameterName = "anotherBuilder",
                    parameterFunction = FunctionMirror
                        .anonymousFunctionMirror()
                        .withReceiverType(emptyBuilder)
                        .withNoReturnType(),
                    nullable = false,
                    InjectBuilderAnnotationMirror(),
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test if one parameter is not having an annotation, an error is thrown`() {
        val emptyBuilder = BuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = BuilderMirrorDsl.builder {
            withAnnotationOnBuilder(WithNewBuilderAnnotationMirror(builderClass = emptyBuilder))
            builderMethod {
                withParameter(
                    parameterName = "conceptId",
                    parameterClassMirror = CommonMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValueAnnotationMirror()
                )
                withParameter(
                    parameterName = "myValue",
                    parameterClassMirror = CommonMirrors.stringClassMirror(),
                    nullable = false,
                    // missing annotation
                )
                withFunctionParameter(
                    parameterName = "builder",
                    parameterFunction = FunctionMirror
                        .anonymousFunctionMirror()
                        .withReceiverType(emptyBuilder)
                        .withNoReturnType(),
                    nullable = false,
                    InjectBuilderAnnotationMirror(),
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test builder inject annotation on a non-last parameter should throw an error`() {
        val emptyBuilder = BuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = BuilderMirrorDsl.builder {
            withAnnotationOnBuilder(WithNewBuilderAnnotationMirror(builderClass = emptyBuilder))
            builderMethod {
                withParameter(
                    parameterName = "conceptId",
                    parameterClassMirror = CommonMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValueAnnotationMirror()
                )
                withFunctionParameter(
                    parameterName = "builder",
                    parameterFunction = FunctionMirror
                        .anonymousFunctionMirror()
                        .withReceiverType(emptyBuilder)
                        .withNoReturnType(),
                    nullable = false,
                    InjectBuilderAnnotationMirror(),
                )
                withParameter(
                    parameterName = "myValue",
                    parameterClassMirror = CommonMirrors.stringClassMirror(),
                    nullable = false,
                    SetFacetValueAnnotationMirror(facetToModify = ClassMirror.interfaceMirror("MyFacetMirror"))
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and ConceptIdentifierValue annotation on same method should throw an error`() {
        val myConceptClass = SchemaMirrorDsl.concept {
            // nothing
        }
        val builder = BuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(NewConceptAnnotationMirror(concept = myConceptClass))
                withParameter(
                    parameterName = "conceptId",
                    parameterClassMirror = CommonMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    IgnoreNullFacetValueAnnotationMirror(),
                    SetConceptIdentifierValueAnnotationMirror(),
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and InjectBuilder annotation on same method should throw an error`() {
        val myConceptClass = SchemaMirrorDsl.concept {
            // nothing
        }
        val emptyBuilder = BuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = BuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(NewConceptAnnotationMirror(concept = myConceptClass))
                withParameter(
                    parameterName = "conceptId",
                    parameterClassMirror = CommonMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValueAnnotationMirror(),
                )
                withFunctionParameter(
                    parameterName = "builder",
                    parameterFunction = FunctionMirror
                        .anonymousFunctionMirror()
                        .withReceiverType(emptyBuilder)
                        .withNoReturnType(),
                    nullable = false,
                    InjectBuilderAnnotationMirror(),
                    IgnoreNullFacetValueAnnotationMirror(),
                )

            }
        }
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test concept id parameter without ConceptIdentifier class should throw an error`() {
        val builder = BuilderMirrorDsl.builder {
            builderMethod {
                withParameter(
                    parameterName = "conceptId",
                    parameterClassMirror = CommonMirrors.stringClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValueAnnotationMirror(),
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

}