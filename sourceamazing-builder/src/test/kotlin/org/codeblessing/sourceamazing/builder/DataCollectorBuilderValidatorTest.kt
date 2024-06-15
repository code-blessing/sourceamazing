package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderException
import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.typemirror.IgnoreNullFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.InjectBuilderAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.NewConceptAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetConceptIdentifierValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.WithNewBuilderAnnotationMirror
import org.codeblessing.sourceamazing.schema.schemacreator.CommonFakeMirrors
import org.codeblessing.sourceamazing.schema.schemacreator.FakeSchemaMirrorDsl
import org.codeblessing.sourceamazing.schema.typemirror.FakeClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.FakeFunctionMirror
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DataCollectorBuilderValidatorTest {

    @Test
    fun `test data collector without annotation should throw an exception`() {
        val builder = FakeBuilderMirrorDsl.builder(addBuilderAnnotation = false) {
            // empty builder without annotation
        }

        assertThrows(DataCollectorBuilderException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test builder class not being an interface should throw an exception`() {
        val builder = FakeBuilderMirrorDsl.builder {
            setBuilderIsClass()
        }
        assertThrows(DataCollectorBuilderException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }


    @Test
    fun `test data collector without accessor method should return without exception`() {
        val builder = FakeBuilderMirrorDsl.builder {
            // builder without methods
        }

        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
    }

    @Test
    fun `test builder method without BuilderMethod annotation should throw an exception`() {
        val builder = FakeBuilderMirrorDsl.builder {
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
        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                // no parameter and return type
            }
        }
        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
    }


    @Test
    fun `test sub-builder class without Builder annotation should throw an exception`() {
        val otherBuilder = FakeBuilderMirrorDsl.builder(addBuilderAnnotation = false) {
            // empty builder without annotation
        }
        val builder = FakeBuilderMirrorDsl.builder {
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
        val emptyBuilder = FakeBuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(WithNewBuilderAnnotationMirror(builderClass = emptyBuilder))
                // no parameter and return type
            }
        }

        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
    }


    @Test
    fun `test builder method without base BuilderMethod annotation should throw an exception`() {
        val emptyBuilder = FakeBuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = FakeBuilderMirrorDsl.builder {
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
        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withFunctionParameter(
                    parameterName = "builder",
                    parameterFunction = FakeFunctionMirror
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
        val emptyBuilder = FakeBuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = FakeBuilderMirrorDsl.builder {
            withAnnotationOnBuilder(WithNewBuilderAnnotationMirror(builderClass = emptyBuilder))
            builderMethod {
                withFunctionParameter(
                    parameterName = "builder",
                    parameterFunction = FakeFunctionMirror
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
        val emptyBuilder = FakeBuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = FakeBuilderMirrorDsl.builder {
            withAnnotationOnBuilder(WithNewBuilderAnnotationMirror(builderClass = emptyBuilder))
            builderMethod {
                withFunctionParameter(
                    parameterName = "builder",
                    parameterFunction = FakeFunctionMirror
                        .anonymousFunctionMirror()
                        .withReceiverType(emptyBuilder)
                        .withNoReturnType(),
                    nullable = false,
                    InjectBuilderAnnotationMirror(),
                )
                withFunctionParameter(
                    parameterName = "anotherBuilder",
                    parameterFunction = FakeFunctionMirror
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
        val emptyBuilder = FakeBuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = FakeBuilderMirrorDsl.builder {
            withAnnotationOnBuilder(WithNewBuilderAnnotationMirror(builderClass = emptyBuilder))
            builderMethod {
                withParameter(
                    parameterName = "conceptId",
                    parameterClassMirror = CommonFakeMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValueAnnotationMirror()
                )
                withParameter(
                    parameterName = "myValue",
                    parameterClassMirror = CommonFakeMirrors.stringClassMirror(),
                    nullable = false,
                    // missing annotation
                )
                withFunctionParameter(
                    parameterName = "builder",
                    parameterFunction = FakeFunctionMirror
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
        val emptyBuilder = FakeBuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = FakeBuilderMirrorDsl.builder {
            withAnnotationOnBuilder(WithNewBuilderAnnotationMirror(builderClass = emptyBuilder))
            builderMethod {
                withParameter(
                    parameterName = "conceptId",
                    parameterClassMirror = CommonFakeMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValueAnnotationMirror()
                )
                withFunctionParameter(
                    parameterName = "builder",
                    parameterFunction = FakeFunctionMirror
                        .anonymousFunctionMirror()
                        .withReceiverType(emptyBuilder)
                        .withNoReturnType(),
                    nullable = false,
                    InjectBuilderAnnotationMirror(),
                )
                withParameter(
                    parameterName = "myValue",
                    parameterClassMirror = CommonFakeMirrors.stringClassMirror(),
                    nullable = false,
                    SetFacetValueAnnotationMirror(facetToModify = FakeClassMirror.interfaceMirror("MyFacetMirror"))
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and ConceptIdentifierValue annotation on same method should throw an error`() {
        val myConceptClass = FakeSchemaMirrorDsl.concept {
            // nothing
        }
        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(NewConceptAnnotationMirror(concept = myConceptClass))
                withParameter(
                    parameterName = "conceptId",
                    parameterClassMirror = CommonFakeMirrors.conceptIdentifierClassMirror(),
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
        val myConceptClass = FakeSchemaMirrorDsl.concept {
            // nothing
        }
        val emptyBuilder = FakeBuilderMirrorDsl.builder {
            // empty builder
        }
        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(NewConceptAnnotationMirror(concept = myConceptClass))
                withParameter(
                    parameterName = "conceptId",
                    parameterClassMirror = CommonFakeMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValueAnnotationMirror(),
                )
                withFunctionParameter(
                    parameterName = "builder",
                    parameterFunction = FakeFunctionMirror
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
        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withParameter(
                    parameterName = "conceptId",
                    parameterClassMirror = CommonFakeMirrors.stringClassMirror(),
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