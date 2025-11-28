package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderDataCardinalityTest {

    private interface MyConcepts {

        interface MyConcept {

            val zeroToMultipleTexts: List<String>
        }

        val concepts: List<MyConcept>
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts::class, conceptAlias = "root")
    private interface BuilderToAddOrReplaceFacets {

        @BuilderMethod
        @NewConcept(concept = MyConcepts.MyConcept::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "concepts",
            referencedConceptAlias = "myConcept",
        )
        fun createConcept(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts.MyConcept::class, conceptAlias = "myConcept")
        interface NestedBuilder {
            @BuilderMethod
            fun addText(
                @SetFacetValue(
                    facetToModify = "zeroToMultipleTexts",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myValue: String
            ): NestedBuilder

            @BuilderMethod
            fun addTexts(
                @SetFacetValue(
                    facetToModify = "zeroToMultipleTexts",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                vararg myValues: String
            ): NestedBuilder
        }
    }

    @Test
    fun `test insert nothing to a text facet will return an empty list`() {
        val schemaInstance =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept()
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(0, concept.zeroToMultipleTexts.size)
    }

    @Test
    fun `test insert four texts individually to a text facet will return an list of four elements`() {
        val schemaInstance =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept().addText("hallo1").addText("hallo2").addText("hallo3").addText("hallo4")
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(4, concept.zeroToMultipleTexts.size)
    }

    @Test
    fun `test insert four texts as array list to a text facet will return an list of four elements`() {
        val schemaInstance =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept().addTexts("hallo1", "hello2", "hallo3", "hallo4")
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(4, concept.zeroToMultipleTexts.size)
    }
}
