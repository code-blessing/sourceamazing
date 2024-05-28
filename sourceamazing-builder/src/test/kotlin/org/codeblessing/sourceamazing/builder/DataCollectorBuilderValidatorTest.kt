package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderException
import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderMethodSyntaxException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DataCollectorBuilderValidatorTest {

    @Builder
    private interface EmptyBuilder


    private interface DataCollectorWithoutAnnotation

    @Test
    fun `test data collector without annotation should throw an exception`() {
        assertThrows(DataCollectorBuilderException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(DataCollectorWithoutAnnotation::class)
        }
    }

    @Builder
    private class DataCollectorNotAnInterface

    @Test
    fun `test builder class not being an interface should throw an exception`() {
        assertThrows(DataCollectorBuilderException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(DataCollectorNotAnInterface::class)
        }
    }


    @Builder
    private interface DataCollectorWithoutMethodsAndBuilders

    @Test
    fun `test data collector without accessor method should return without exception`() {
        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
            DataCollectorWithoutMethodsAndBuilders::class)
    }

    @Builder
    private interface DataCollectorWithMethodWithoutAnnotations {

        fun doSomething()
    }

    @Test
    fun `test builder method without BuilderMethod annotation should throw an exception`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithMethodWithoutAnnotations::class)
        }
    }

    @Builder
    private interface DataCollectorWithSameBuilderWithAnnotation {

        @BuilderMethod
        fun doSomething()
    }

    @Test
    fun `test data collector with BuilderMethod annotation using the same builder should return without exception`() {
        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
            DataCollectorWithSameBuilderWithAnnotation::class)
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
        assertThrows(DataCollectorBuilderException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithOtherBuildersWithoutAnnotation::class)
        }
    }


    @Builder
    private interface DataCollectorWithOtherBuildersWithAnnotation {

        @BuilderMethod
        @WithNewBuilder(builderClass = EmptyBuilder::class)
        fun doSomething()
    }

    @Test
    fun `test data collector sub-builder with annotation should return without exception`() {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithOtherBuildersWithAnnotation::class)
    }


    @Builder
    private interface DataCollectorMethodWithoutBaseAnnotation {

        @WithNewBuilder(builderClass = EmptyBuilder::class)
        fun doSomething()
    }

    @Test
    fun `test builder method without base BuilderMethod annotation should throw an exception`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorMethodWithoutBaseAnnotation::class)
        }
    }

    @Builder
    private interface DataCollectorBuilderInjectionWithoutDeclaringNewBuilderAnnotation {

        @BuilderMethod
        fun doSomething(
            @InjectBuilder builder: DataCollectorBuilderInjectionWithoutDeclaringNewBuilderAnnotation.() -> Unit,
        )
    }

    @Test
    fun `test builder injection without declaring the builder should use the existing builder and return without exception`() {
        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
            DataCollectorBuilderInjectionWithoutDeclaringNewBuilderAnnotation::class
        )
    }


    @Builder
    private interface DataCollectorWithBuilderParameter {

        @BuilderMethod
        @WithNewBuilder(builderClass = EmptyBuilder::class)
        fun doSomething(
            @InjectBuilder builder: EmptyBuilder.() -> Unit,
        )
    }

    @Test
    fun `test builder methods last parameter having the builder injection annotation should return without exception`() {
        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(DataCollectorWithBuilderParameter::class)
    }

    @Builder
    private interface DataCollectorWithTwoBuilderParameter {

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
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithTwoBuilderParameter::class)
        }
    }

    @Builder
    private interface DataCollectorWithMissingParameterAnnotation {

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
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithMissingParameterAnnotation::class)
        }
    }

    @Builder
    private interface DataCollectorWithBuilderInjectionOnNonLastParam {
        interface MyFacetValue

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
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithBuilderInjectionOnNonLastParam::class)
        }
    }

    @Builder
    private interface DataCollectorWithIllegalConceptIdClass {

        @BuilderMethod
        fun doSomething(
            @SetConceptIdentifierValue conceptId: String,
        )
    }

    @Test
    fun `test concept id parameter without ConceptIdentifier class should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithIllegalConceptIdClass::class)
        }
    }

}