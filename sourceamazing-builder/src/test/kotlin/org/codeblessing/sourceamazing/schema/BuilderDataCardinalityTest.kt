package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.WrongCardinalityForFacetValueException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderDataCardinalityTest {

    @Schema(concepts = [SchemaWithConceptWithFacet.ConceptWithFacet::class])
    private interface SchemaWithConceptWithFacet {

        @Concept(facets = [
            ConceptWithFacet.ListOfZeroToThreeTextFacet::class,
        ])
        interface ConceptWithFacet {
            @StringFacet(minimumOccurrences = 1, maximumOccurrences = 3)
            interface ListOfZeroToThreeTextFacet

            @QueryFacetValue(ListOfZeroToThreeTextFacet::class)
            fun getTextFacetAsList(): List<String>

        }

        @QueryConcepts(conceptClasses = [ConceptWithFacet::class])
        fun getConcepts(): List<ConceptWithFacet>
    }


    @Builder
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
                    facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.ListOfZeroToThreeTextFacet::class,
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myValue: String,
            ): NestedBuilder

            @BuilderMethod
            fun addTexts(
                @SetFacetValue(
                    facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.ListOfZeroToThreeTextFacet::class,
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                vararg myValues: String,
            ): NestedBuilder

        }
    }

    @Test
    fun `test insert a correct amount of facet entries does not throw validation exception`() {
        val schemaInstance: SchemaWithConceptWithFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceFacets::class) { builder ->
                builder.createConcept()
                    .addText("hallo1")
            }
        }

        val concept = schemaInstance.getConcepts().first()
        Assertions.assertEquals(1, concept.getTextFacetAsList().size)
    }

    @Test
    fun `test insert nothing to a text facet with minimumOccurrences of 1 throws an exception`() {
        assertThrows<WrongCardinalityForFacetValueException> {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceFacets::class) { builder ->
                    builder.createConcept()
                }
            }
        }
    }

    @Test
    fun `test insert four texts to a text facet with maximumOccurrences of 3 throws an exception`() {
        assertThrows<WrongCardinalityForFacetValueException> {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceFacets::class) { builder ->
                    builder.createConcept()
                    .addText("hallo1")
                    .addText("hallo2")
                    .addText("hallo3")
                    .addText("hallo4")
                }
            }
        }
    }

    @Test
    fun `test insert four texts as array list to a text facet with maximumOccurrences of 3 throws an exception`() {
        assertThrows<WrongCardinalityForFacetValueException> {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceFacets::class) { builder ->
                    builder.createConcept()
                        .addTexts("hallo1", "hello2", "hallo3", "hallo4")
                }
            }
        }
    }

}