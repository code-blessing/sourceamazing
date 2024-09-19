package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
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
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.fakereflection.FakeKClass
import org.codeblessing.sourceamazing.schema.schemacreator.CommonFakeMirrors
import org.codeblessing.sourceamazing.schema.schemacreator.FakeSchemaMirrorDsl
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DataCollectorBuilderValidatorMethodCompletenessTest {
    // Do not validate for duplicate assignment on the same facet and concept, as this can
    // be a useful case when adding values to a facet (instead of replacing values).

    @Test
    fun `test duplicate alias for NewConcept annotation should throw an error`() {
        val myConcept = FakeSchemaMirrorDsl.concept {
            // empty concept
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(NewConcept(concept = myConcept, declareConceptAlias = "foo"))
                withAnnotationOnMethod(NewConcept(concept = myConcept, declareConceptAlias = "foo"))
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test duplicate alias from superior concept for NewConcept annotation should throw an error`() {
        val myConcept = FakeSchemaMirrorDsl.concept {
            // empty concept
        }

        val builder = FakeBuilderMirrorDsl.builder {
            withAnnotationOnBuilder(ExpectedAliasFromSuperiorBuilder(conceptAlias = "foo"))
            builderMethod {
                withAnnotationOnMethod(NewConcept(concept = myConcept, declareConceptAlias = "foo"))
                withAnnotationOnMethod(SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo"))
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test duplicate alias for AutoRandomConceptIdentifier annotation should throw an error`() {
        val myConcept = FakeSchemaMirrorDsl.concept {
            // empty concept
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(NewConcept(concept = myConcept, declareConceptAlias = "foo"))
                withAnnotationOnMethod(SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo"))
                withAnnotationOnMethod(SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo"))
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test duplicate alias for ConceptIdentifierValue annotation should throw an error`() {
        val myConcept = FakeSchemaMirrorDsl.concept {
            // empty concept
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(NewConcept(concept = myConcept, declareConceptAlias = "foo"))
                withMethodName("doSomething")
                withParameter(
                    parameterName = "conceptIdentifier1",
                    parameterClassMirror = CommonFakeMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValue(conceptToModifyAlias = "foo"),
                )
                withParameter(
                    parameterName = "conceptIdentifier2",
                    parameterClassMirror = CommonFakeMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValue(conceptToModifyAlias = "foo"),
                )
            }
        }
        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test missing concept identifier declaration for alias should throw an error`() {
        val myConcept = FakeSchemaMirrorDsl.concept {
            // empty concept
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(NewConcept(concept = myConcept, declareConceptAlias = "foo"))
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test duplicate alias for AutoRandomConceptIdentifier and ConceptIdentifier annotation should throw an error`() {
        val myConcept = FakeSchemaMirrorDsl.concept {
            // empty concept
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(NewConcept(concept = myConcept, declareConceptAlias = "foo"))
                withAnnotationOnMethod(SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo"))
                withParameter(
                    parameterName = "conceptIdentifier",
                    parameterClassMirror = CommonFakeMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValue(conceptToModifyAlias = "foo"),
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias on AutoRandomConceptIdentifier annotation should throw an error`() {
        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(SetRandomConceptIdentifierValue(conceptToModifyAlias = "unknown"))
            }
        }


        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias on ConceptIdentifierValue annotation should throw an error`() {
        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withParameter(
                    parameterName = "id",
                    parameterClassMirror = CommonFakeMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValue(conceptToModifyAlias = "unknown"),
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias on FacetValue annotation should throw an error`() {
        lateinit var myFacet : FakeKClass
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(StringFacet())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withParameter(
                    parameterName = "value",
                    parameterClassMirror = CommonFakeMirrors.stringClassMirror(),
                    nullable = false,
                    SetFacetValue(
                        conceptToModifyAlias = "unknown",
                        facetToModify = myFacet,
                    ),
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias in property conceptToModifyAlias on the ReferenceFacetValue annotation should throw an error`() {
        lateinit var myFacet : FakeKClass
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(StringFacet())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            withAnnotationOnBuilder(ExpectedAliasFromSuperiorBuilder(conceptAlias = "known"))
            builderMethod {
                withAnnotationOnMethod(SetAliasConceptIdentifierReferenceFacetValue(
                    conceptToModifyAlias = "unknown",
                    facetToModify = myFacet,
                    referencedConceptAlias = "known",
                    facetModificationRule = FacetModificationRule.ADD
                ))
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias in property referencedConceptAlias on ReferenceFacetValue annotation should throw an error`() {
        lateinit var myFacet : FakeKClass
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(StringFacet())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            withAnnotationOnBuilder(ExpectedAliasFromSuperiorBuilder(conceptAlias = "known"))
            builderMethod {
                withAnnotationOnMethod(
                    SetAliasConceptIdentifierReferenceFacetValue(
                    conceptToModifyAlias = "known",
                    facetToModify = myFacet,
                    referencedConceptAlias = "unknown",
                    facetModificationRule = FacetModificationRule.ADD
                )
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias on DefaultStringFacetValue annotation should throw an error`() {
        lateinit var myFacet : FakeKClass
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(StringFacet())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(
                    SetFixedStringFacetValue(
                    conceptToModifyAlias = "unknown",
                    facetToModify = myFacet,
                    facetModificationRule = FacetModificationRule.ADD,
                    value = "foo"
                )
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias on DefaultBooleanFacetValue annotation should throw an error`() {
        lateinit var myFacet : FakeKClass
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(BooleanFacet())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(
                    SetFixedBooleanFacetValue(
                    conceptToModifyAlias = "unknown",
                    facetToModify = myFacet,
                    facetModificationRule = FacetModificationRule.ADD,
                    value = false
                )
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias on DefaultIntFacetValue annotation should throw an error`() {
        lateinit var myFacet : FakeKClass
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(IntFacet())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(
                    SetFixedIntFacetValue(
                    conceptToModifyAlias = "unknown",
                    facetToModify = myFacet,
                    facetModificationRule = FacetModificationRule.ADD,
                    value = 42
                )
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias on DefaultEnumFacetValue annotation should throw an error`() {
        lateinit var myFacet : FakeKClass
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(EnumFacet(CommonFakeMirrors.enumClassMirror("FOO", "BAR")))
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(
                    SetFixedEnumFacetValue(
                    conceptToModifyAlias = "unknown",
                    facetToModify = myFacet,
                    facetModificationRule = FacetModificationRule.ADD,
                    value = "BAR"
                )
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder should return without exceptions`() {
        lateinit var myFacet : FakeKClass
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(StringFacet())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            withAnnotationOnBuilder(ExpectedAliasFromSuperiorBuilder(conceptAlias = "known"))
            withAnnotationOnBuilder(ExpectedAliasFromSuperiorBuilder(conceptAlias = "alsoKnown"))
            builderMethod {
                withParameter(
                    parameterName = "value",
                    parameterClassMirror = CommonFakeMirrors.stringClassMirror(),
                    nullable = false,
                    SetFacetValue(
                        conceptToModifyAlias = "known",
                        facetToModify = myFacet,
                    )
                )
            }
        }

        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
    }
}