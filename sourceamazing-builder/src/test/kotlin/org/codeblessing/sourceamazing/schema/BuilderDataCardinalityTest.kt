package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.WrongCardinalityForFacetValueException
import org.codeblessing.sourceamazing.toConceptName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderDataCardinalityTest {

    private interface SchemaWithConceptWithFacet {

        interface ConceptWithFacet {

            @Facet
            val zeroToThreeTexts: List<String>
        }

        @Facet
        val concepts: List<ConceptWithFacet>
    }


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderToAddOrReplaceFacets {

        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "myConcept")
        fun createConcept(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "myConcept")
        interface NestedBuilder {
            @BuilderMethod
            fun addText(
                @SetFacetValue(
                    facetToModify = "zeroToThreeTexts",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myValue: String,
            ): NestedBuilder

            @BuilderMethod
            fun addTexts(
                @SetFacetValue(
                    facetToModify = "zeroToThreeTexts",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                vararg myValues: String,
            ): NestedBuilder

        }
    }

    @Test
    fun `test insert a correct amount of facet entries does not throw validation exception`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderToAddOrReplaceFacets::class,
                    ) { builder ->
                        builder.createConcept()
                            .addText("hallo1")
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(1, concept.zeroToThreeTexts.size)
    }

    @Test
    fun `test insert nothing to a text facet with minimumOccurrences of 1 throws an exception`() {
        assertThrows<WrongCardinalityForFacetValueException> {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderToAddOrReplaceFacets::class,
                    ) { builder ->
                        builder.createConcept()
                    }
                }
            }
        }
    }

    @Test
    fun `test insert four texts to a text facet with maximumOccurrences of 3 throws an exception`() {
        assertThrows<WrongCardinalityForFacetValueException> {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderToAddOrReplaceFacets::class,
                    ) { builder ->
                        builder.createConcept()
                            .addText("hallo1")
                            .addText("hallo2")
                            .addText("hallo3")
                            .addText("hallo4")
                    }
                }
            }
        }
    }

    @Test
    fun `test insert four texts as array list to a text facet with maximumOccurrences of 3 throws an exception`() {
        assertThrows<WrongCardinalityForFacetValueException> {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderToAddOrReplaceFacets::class,
                    ) { builder ->
                        builder.createConcept()
                            .addTexts("hallo1", "hello2", "hallo3", "hallo4")
                    }
                }
            }
        }
    }

}
