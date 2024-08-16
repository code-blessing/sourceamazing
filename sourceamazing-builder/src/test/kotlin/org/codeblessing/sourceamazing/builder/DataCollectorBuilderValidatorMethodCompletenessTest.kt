package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.typemirror.ExpectedAliasFromSuperiorBuilderAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.NewConceptAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetAliasConceptIdentifierReferenceFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetConceptIdentifierValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFixedBooleanFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFixedEnumFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFixedIntFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFixedStringFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetRandomConceptIdentifierValueAnnotationMirror
import org.codeblessing.sourceamazing.schema.schemacreator.CommonFakeMirrors
import org.codeblessing.sourceamazing.schema.schemacreator.FakeSchemaMirrorDsl
import org.codeblessing.sourceamazing.schema.typemirror.BooleanFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.EnumFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.FakeClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.IntFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.StringFacetAnnotationMirror
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
                withAnnotationOnMethod(NewConceptAnnotationMirror(concept = myConcept, declareConceptAlias = "foo"))
                withAnnotationOnMethod(NewConceptAnnotationMirror(concept = myConcept, declareConceptAlias = "foo"))
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
            withAnnotationOnBuilder(ExpectedAliasFromSuperiorBuilderAnnotationMirror(conceptAlias = "foo"))
            builderMethod {
                withAnnotationOnMethod(NewConceptAnnotationMirror(concept = myConcept, declareConceptAlias = "foo"))
                withAnnotationOnMethod(SetRandomConceptIdentifierValueAnnotationMirror(conceptToModifyAlias = "foo"))
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
                withAnnotationOnMethod(NewConceptAnnotationMirror(concept = myConcept, declareConceptAlias = "foo"))
                withAnnotationOnMethod(SetRandomConceptIdentifierValueAnnotationMirror(conceptToModifyAlias = "foo"))
                withAnnotationOnMethod(SetRandomConceptIdentifierValueAnnotationMirror(conceptToModifyAlias = "foo"))
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
                withAnnotationOnMethod(NewConceptAnnotationMirror(concept = myConcept, declareConceptAlias = "foo"))
                withMethodName("doSomething")
                withParameter(
                    parameterName = "conceptIdentifier1",
                    parameterClassMirror = CommonFakeMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValueAnnotationMirror(conceptToModifyAlias = "foo"),
                )
                withParameter(
                    parameterName = "conceptIdentifier2",
                    parameterClassMirror = CommonFakeMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValueAnnotationMirror(conceptToModifyAlias = "foo"),
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
                withAnnotationOnMethod(NewConceptAnnotationMirror(concept = myConcept, declareConceptAlias = "foo"))
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
                withAnnotationOnMethod(NewConceptAnnotationMirror(concept = myConcept, declareConceptAlias = "foo"))
                withAnnotationOnMethod(SetRandomConceptIdentifierValueAnnotationMirror(conceptToModifyAlias = "foo"))
                withParameter(
                    parameterName = "conceptIdentifier",
                    parameterClassMirror = CommonFakeMirrors.conceptIdentifierClassMirror(),
                    nullable = false,
                    SetConceptIdentifierValueAnnotationMirror(conceptToModifyAlias = "foo"),
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
                withAnnotationOnMethod(SetRandomConceptIdentifierValueAnnotationMirror(conceptToModifyAlias = "unknown"))
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
                    SetConceptIdentifierValueAnnotationMirror(conceptToModifyAlias = "unknown"),
                )
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias on FacetValue annotation should throw an error`() {
        lateinit var myFacet : FakeClassMirror
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(StringFacetAnnotationMirror())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withParameter(
                    parameterName = "value",
                    parameterClassMirror = CommonFakeMirrors.stringClassMirror(),
                    nullable = false,
                    SetFacetValueAnnotationMirror(
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
        lateinit var myFacet : FakeClassMirror
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(StringFacetAnnotationMirror())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            withAnnotationOnBuilder(ExpectedAliasFromSuperiorBuilderAnnotationMirror(conceptAlias = "known"))
            builderMethod {
                withAnnotationOnMethod(SetAliasConceptIdentifierReferenceFacetValueAnnotationMirror(
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
        lateinit var myFacet : FakeClassMirror
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(StringFacetAnnotationMirror())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            withAnnotationOnBuilder(ExpectedAliasFromSuperiorBuilderAnnotationMirror(conceptAlias = "known"))
            builderMethod {
                withAnnotationOnMethod(SetAliasConceptIdentifierReferenceFacetValueAnnotationMirror(
                    conceptToModifyAlias = "known",
                    facetToModify = myFacet,
                    referencedConceptAlias = "unknown",
                    facetModificationRule = FacetModificationRule.ADD
                ))
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias on DefaultStringFacetValue annotation should throw an error`() {
        lateinit var myFacet : FakeClassMirror
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(StringFacetAnnotationMirror())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(SetFixedStringFacetValueAnnotationMirror(
                    conceptToModifyAlias = "unknown",
                    facetToModify = myFacet,
                    facetModificationRule = FacetModificationRule.ADD,
                    value = "foo"
                ))
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias on DefaultBooleanFacetValue annotation should throw an error`() {
        lateinit var myFacet : FakeClassMirror
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(BooleanFacetAnnotationMirror())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(SetFixedBooleanFacetValueAnnotationMirror(
                    conceptToModifyAlias = "unknown",
                    facetToModify = myFacet,
                    facetModificationRule = FacetModificationRule.ADD,
                    value = false
                ))
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias on DefaultIntFacetValue annotation should throw an error`() {
        lateinit var myFacet : FakeClassMirror
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(IntFacetAnnotationMirror())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(SetFixedIntFacetValueAnnotationMirror(
                    conceptToModifyAlias = "unknown",
                    facetToModify = myFacet,
                    facetModificationRule = FacetModificationRule.ADD,
                    value = 42
                ))
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of unknown alias on DefaultEnumFacetValue annotation should throw an error`() {
        lateinit var myFacet : FakeClassMirror
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(EnumFacetAnnotationMirror(CommonFakeMirrors.enumClassMirror("FOO", "BAR")))
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            builderMethod {
                withAnnotationOnMethod(SetFixedEnumFacetValueAnnotationMirror(
                    conceptToModifyAlias = "unknown",
                    facetToModify = myFacet,
                    facetModificationRule = FacetModificationRule.ADD,
                    value = "BAR"
                ))
            }
        }

        assertThrows(DataCollectorBuilderMethodSyntaxException::class.java) {
            DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
        }
    }

    @Test
    fun `test use of alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder should return without exceptions`() {
        lateinit var myFacet : FakeClassMirror
        FakeSchemaMirrorDsl.concept {
            myFacet = facet {
                withAnnotationOnFacet(StringFacetAnnotationMirror())
            }
        }

        val builder = FakeBuilderMirrorDsl.builder {
            withAnnotationOnBuilder(ExpectedAliasFromSuperiorBuilderAnnotationMirror(conceptAlias = "known"))
            withAnnotationOnBuilder(ExpectedAliasFromSuperiorBuilderAnnotationMirror(conceptAlias = "alsoKnown"))
            builderMethod {
                withParameter(
                    parameterName = "value",
                    parameterClassMirror = CommonFakeMirrors.stringClassMirror(),
                    nullable = false,
                    SetFacetValueAnnotationMirror(
                        conceptToModifyAlias = "known",
                        facetToModify = myFacet,
                    )
                )
            }
        }

        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(builder)
    }
}