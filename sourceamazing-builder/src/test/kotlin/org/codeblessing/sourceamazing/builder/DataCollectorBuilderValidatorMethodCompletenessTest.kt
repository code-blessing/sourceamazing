package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderMethodSyntaxException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DataCollectorBuilderValidatorMethodCompletenessTest {
    // Do not validate for duplicate assignment on the same facet and concept, as this can
    // be a useful case when adding values to a facet (instead of replacing values).

    @Builder
    private interface DataCollectorWithDuplicateAliasForNewConcept {
        interface MyConceptClass

        @BuilderMethod
        @NewConcept(MyConceptClass::class, declareConceptAlias = "foo")
        @NewConcept(MyConceptClass::class, declareConceptAlias = "foo")
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
        interface MyConceptClass

        @BuilderMethod
        @NewConcept(MyConceptClass::class, declareConceptAlias = "foo")
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
        interface MyConceptClass

        @BuilderMethod
        @NewConcept(MyConceptClass::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test duplicate alias for AutoRandomConceptIdentifier annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithDuplicateAliasForRandomConceptIdentifier::class)
        }
    }

    @Builder
    private interface DataCollectorWithDuplicateAliasForManuallySetConceptIdentifier {
        interface MyConceptClass

        @BuilderMethod
        @NewConcept(MyConceptClass::class, declareConceptAlias = "foo")
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
        interface MyConceptClass

        @BuilderMethod
        @NewConcept(MyConceptClass::class, declareConceptAlias = "foo")
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
        interface MyConceptClass

        @BuilderMethod
        @NewConcept(MyConceptClass::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier: ConceptIdentifier
        )
    }

    @Test
    fun `test duplicate alias for AutoRandomConceptIdentifier and ConceptIdentifier annotation should throw an error`() {
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
    fun `test use of unknown alias on AutoRandomConceptIdentifier annotation should throw an error`() {
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
    fun `test use of unknown alias on ConceptIdentifierValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInConceptIdentifierValueAnnotation::class)
        }
    }

    @Builder
    private interface DataCollectorWithUseOfUnknownAliasInFacetValueAnnotation {

        interface FacetClass
        @BuilderMethod
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "unknown", facetToModify = FacetClass::class) value: String
        )
    }

    @Test
    fun `test use of unknown alias on FacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInFacetValueAnnotation::class)
        }
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder("known")
    private interface DataCollectorWithUseOfUnknownAliasInLinkFacetAnnotation {
        interface FacetClass
        @BuilderMethod
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "unknown", facetToModify = FacetClass::class, referencedConceptAlias = "known")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias in property conceptToModifyAlias on the ReferenceFacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInLinkFacetAnnotation::class)
        }
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder("known")
    private interface DataCollectorWithUseOfUnknownAliasInLinkFacetReferenceAnnotation {
        interface FacetClass
        @BuilderMethod
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "known", facetToModify = FacetClass::class, referencedConceptAlias = "unknown")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias in property referencedConceptAlias on ReferenceFacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInLinkFacetReferenceAnnotation::class)
        }
    }

    @Builder
    private interface DataCollectorWithUseOfUnknownAliasInDefaultStringFacetValueAnnotation {
        interface FacetClass
        @BuilderMethod
        @SetFixedStringFacetValue(conceptToModifyAlias = "unknown", facetToModify = FacetClass::class, value = "foo")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on DefaultStringFacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInDefaultStringFacetValueAnnotation::class)
        }
    }

    @Builder
    private interface DataCollectorWithUseOfUnknownAliasInDefaultBooleanFacetValueAnnotation {
        interface FacetClass
        @BuilderMethod
        @SetFixedBooleanFacetValue(conceptToModifyAlias = "unknown", facetToModify = FacetClass::class, value = false)
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on DefaultBooleanFacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInDefaultBooleanFacetValueAnnotation::class)
        }
    }

    @Builder
    private interface DataCollectorWithUseOfUnknownAliasInDefaultIntegerFacetValueAnnotation {
        interface FacetClass
        @BuilderMethod
        @SetFixedIntFacetValue(conceptToModifyAlias = "unknown", facetToModify = FacetClass::class, value = 42)
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on DefaultIntFacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInDefaultIntegerFacetValueAnnotation::class)
        }
    }

    @Builder
    private interface DataCollectorWithUseOfUnknownAliasInDefaultEnumFacetValueAnnotation {
        interface FacetClass
        @BuilderMethod
        @SetFixedEnumFacetValue(conceptToModifyAlias = "unknown", facetToModify = FacetClass::class, value = "BAR")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on DefaultEnumFacetValue annotation should throw an error`() {
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
                DataCollectorWithUseOfUnknownAliasInDefaultEnumFacetValueAnnotation::class)
        }
    }


    @Builder
    @ExpectedAliasFromSuperiorBuilder(conceptAlias = "known")
    @ExpectedAliasFromSuperiorBuilder(conceptAlias = "alsoKnown")
    private interface DataCollectorMissingAnAliasButExpectsItFromParentBuilder {
        interface FacetClass
        @BuilderMethod
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "known", facetToModify = FacetClass::class) value: String,
        )
    }

    @Test
    fun `test use of alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder should return without exceptions`() {
        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(
            DataCollectorMissingAnAliasButExpectsItFromParentBuilder::class)
    }
}