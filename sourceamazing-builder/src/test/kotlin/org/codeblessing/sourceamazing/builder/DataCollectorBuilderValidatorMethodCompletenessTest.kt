package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedBooleanFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedEnumFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedIntFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedStringFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DataCollectorBuilderValidatorMethodCompletenessTest {
    // Do not validate for duplicate assignment on the same facet and concept, as this can
    // be a useful case when adding values to a facet (instead of replacing values).

    @Schema(concepts = [MyConcept::class])
    private interface MySchema

    @Concept(facets = [MyFacet::class])
    private interface MyConcept

    @StringFacet
    private interface MyFacet

    // tests

    @Builder
    private interface DataCollectorWithDuplicateAliasForNewConcept {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test duplicate alias for NewConcept annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithDuplicateAliasForNewConcept::class)
        }
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder("foo")
    private interface DataCollectorWithDuplicateAliasImportedFromSuperiorForNewConcept {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test duplicate alias from superior concept for NewConcept annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithDuplicateAliasImportedFromSuperiorForNewConcept::class)
        }
    }

    @Builder
    private interface DataCollectorWithDuplicateAliasForRandomConceptIdentifier {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test duplicate alias for SetRandomConceptIdentifierValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithDuplicateAliasForRandomConceptIdentifier::class)
        }
    }

    @Builder
    private interface DataCollectorWithDuplicateAliasForManuallySetConceptIdentifier {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier1: ConceptIdentifier,
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier2: ConceptIdentifier,
        )
    }

    @Test
    fun `test duplicate alias for ConceptIdentifierValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithDuplicateAliasForManuallySetConceptIdentifier::class)
        }
    }

    @Builder
    private interface DataCollectorWithoutConceptIdentifierForAlias {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test missing concept identifier declaration for alias should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithoutConceptIdentifierForAlias::class)
        }
    }


    @Builder
    private interface DataCollectorWithDuplicateMixedConceptIdentifier {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier: ConceptIdentifier
        )
    }

    @Test
    fun `test duplicate alias with SetRandomConceptIdentifierValue and SetConceptIdentifierValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithDuplicateMixedConceptIdentifier::class)
        }
    }

    @Builder
    private interface DataCollectorWithUseOfUnknownAliasInAutoRandomConceptIdentifier {
        @BuilderMethod
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "unknown")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetRandomConceptIdentifierValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInAutoRandomConceptIdentifier::class)
        }
    }

    @Builder
    private interface DataCollectorWithUseOfUnknownAliasInConceptIdentifierValueAnnotation {
        @BuilderMethod
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "unknown") id: ConceptIdentifier
        )
    }

    @Test
    fun `test use of unknown alias on SetConceptIdentifierValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInConceptIdentifierValueAnnotation::class)
        }
    }

    @Builder
    private interface DataCollectorWithUseOfUnknownAliasInFacetValueAnnotation {

        @BuilderMethod
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "unknown", facetToModify = MyFacet::class) value: String
        )
    }

    @Test
    fun `test use of unknown alias on SetFacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInFacetValueAnnotation::class)
        }
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder("known")
    private interface DataCollectorWithUseOfUnknownAliasInLinkFacetAnnotation {
        @BuilderMethod
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "unknown", facetToModify = MyFacet::class, referencedConceptAlias = "known")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias in property conceptToModifyAlias on the SetAliasConceptIdentifierReferenceFacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInLinkFacetAnnotation::class)
        }
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder("known")
    private interface DataCollectorWithUseOfUnknownAliasInLinkFacetReferenceAnnotation {
        @BuilderMethod
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "known", facetToModify = MyFacet::class, referencedConceptAlias = "unknown")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias in property referencedConceptAlias on SetAliasConceptIdentifierReferenceFacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInLinkFacetReferenceAnnotation::class)
        }
    }

    @Builder
    private interface DataCollectorWithUseOfUnknownAliasInDefaultStringFacetValueAnnotation {
        @BuilderMethod
        @SetFixedStringFacetValue(conceptToModifyAlias = "unknown", facetToModify = MyFacet::class, value = "foo")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedStringFacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInDefaultStringFacetValueAnnotation::class)
        }
    }

    @Builder
    private interface DataCollectorWithUseOfUnknownAliasInDefaultBooleanFacetValueAnnotation {
        @BuilderMethod
        @SetFixedBooleanFacetValue(conceptToModifyAlias = "unknown", facetToModify = MyFacet::class, value = false)
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedBooleanFacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInDefaultBooleanFacetValueAnnotation::class)
        }
    }

    @Builder
    private interface DataCollectorWithUseOfUnknownAliasInDefaultIntegerFacetValueAnnotation {
        @BuilderMethod
        @SetFixedIntFacetValue(conceptToModifyAlias = "unknown", facetToModify = MyFacet::class, value = 42)
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedIntFacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInDefaultIntegerFacetValueAnnotation::class)
        }
    }

    @Builder
    private interface DataCollectorWithUseOfUnknownAliasInDefaultEnumFacetValueAnnotation {
        @BuilderMethod
        @SetFixedEnumFacetValue(conceptToModifyAlias = "unknown", facetToModify = MyFacet::class, value = "BAR")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedEnumFacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInDefaultEnumFacetValueAnnotation::class)
        }
    }


    @Builder
    @ExpectedAliasFromSuperiorBuilder(conceptAlias = "known")
    @ExpectedAliasFromSuperiorBuilder(conceptAlias = "alsoKnown")
    private interface DataCollectorMissingAnAliasButExpectsItFromParentBuilder {
        @BuilderMethod
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "known", facetToModify = MyFacet::class) value: String,
        )
    }

    @Test
    fun `test use of alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder annotation should return without exceptions`() {
        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
            DataCollectorMissingAnAliasButExpectsItFromParentBuilder::class)
    }
}