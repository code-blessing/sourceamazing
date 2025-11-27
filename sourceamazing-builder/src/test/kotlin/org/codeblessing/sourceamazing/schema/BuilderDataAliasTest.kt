package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataAliasTest {

    private interface MyConcepts {

        interface MyConcept {
            val text: String

            val number: Int
        }

        val concepts: List<MyConcept>
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts::class, conceptAlias = "root")
    private interface BuilderUsingSameAliasForSameConceptInNestedBuilders {

        @BuilderMethod
        @NewConcept(concept = MyConcepts.MyConcept::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue("myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "concepts",
            referencedConceptAlias = "myConcept",
        )
        fun createConcept(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(
            concept = MyConcepts.MyConcept::class,
            conceptAlias = "myConcept",
        )
        interface NestedBuilder {
            @BuilderMethod
            fun setText(
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "text")
                textValue: String
            ): NestedSubBuilder
        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder(
            concept = MyConcepts.MyConcept::class,
            conceptAlias = "myConcept",
        )
        interface NestedSubBuilder {

            @BuilderMethod
            fun setNumber(
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "number")
                numberValue: Int
            )
        }
    }

    @Test
    fun `test using the same alias in a sub-builder and a sub-sub-builder`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderUsingSameAliasForSameConceptInNestedBuilders::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept().setText("myText").setNumber(17)
                    }
                }
            }
        assertEquals(1, schemaInstance.concepts.size)

        val myConcept = schemaInstance.concepts.first()
        assertEquals(17, myConcept.number)
        assertEquals("myText", myConcept.text)
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts::class, conceptAlias = "root")
    private interface BuilderUsingSameAliasForTwoDifferentConceptsOnDifferentBuilderLevels {

        @BuilderMethod
        @NewConcept(concept = MyConcepts.MyConcept::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue("myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "concepts",
            referencedConceptAlias = "myConcept",
        )
        fun createConcept(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts::class, conceptAlias = "root")
        @ExpectedAliasFromSuperiorBuilder(
            concept = MyConcepts.MyConcept::class,
            conceptAlias = "myConcept",
        )
        interface NestedBuilder {
            @BuilderMethod
            @SetFixedIntFacetValue(
                conceptToModifyAlias = "myConcept",
                facetToModify = "number",
                value = 42,
            )
            fun setTextAndFixedNumber(
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "text")
                textValue: String
            ): NestedSubBuilder
        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts::class, conceptAlias = "root")
        // no ExpectedAliasFromSuperiorBuilder("myConcept) here, therefore "myConcept" is a new
        // alias
        interface NestedSubBuilder {
            @BuilderMethod
            @NewConcept(concept = MyConcepts.MyConcept::class, declareConceptAlias = "myConcept")
            @SetRandomConceptIdentifierValue("myConcept")
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "root",
                facetToModify = "concepts",
                referencedConceptAlias = "myConcept",
            )
            fun createConceptAndSetText(
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "text")
                textValue: String
            ): NestedSubSubBuilder
        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder(
            concept = MyConcepts.MyConcept::class,
            conceptAlias = "myConcept",
        )
        interface NestedSubSubBuilder {
            @BuilderMethod
            fun setNumber(
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "number")
                numberValue: Int
            )
        }
    }

    @Test
    fun `test using the same alias in a sub-builder for a new concept as no ExpectedAliasFromSuperiorBuilder annotation is declared on the sub-builder`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderUsingSameAliasForTwoDifferentConceptsOnDifferentBuilderLevels::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder
                            .createConcept()
                            .setTextAndFixedNumber("ConceptFromTopLevelBuilder")
                            .createConceptAndSetText("OtherConceptFromSubBuilder")
                            .setNumber(17)
                    }
                }
            }
        assertEquals(2, schemaInstance.concepts.size)

        val firstConcept = schemaInstance.concepts.first()

        assertEquals(42, firstConcept.number)
        assertEquals("ConceptFromTopLevelBuilder", firstConcept.text)

        val secondConcept = schemaInstance.concepts.last()
        assertEquals(17, secondConcept.number)
        assertEquals("OtherConceptFromSubBuilder", secondConcept.text)
    }
}
