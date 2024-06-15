package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedIntFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataAliasTest {

    @Schema(concepts = [
        SchemaWithConceptWithFacet.ConceptWithFacet::class,
    ])
    private interface SchemaWithConceptWithFacet {

        @Concept(facets = [
            ConceptWithFacet.TextFacet::class,
            ConceptWithFacet.NumberFacet::class,
        ])
        interface ConceptWithFacet {

            @StringFacet
            interface TextFacet

            @IntFacet
            interface NumberFacet


            @QueryFacetValue(TextFacet::class)
            fun getText(): String

            @QueryFacetValue(NumberFacet::class)
            fun getNumber(): Int

        }
        @QueryConcepts(conceptClasses = [ConceptWithFacet::class])
        fun getConcepts(): List<ConceptWithFacet>
    }

    @Builder
    private interface BuilderUsingSameAliasForSameConceptInNestedBuilders {

        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue("myConcept")
        fun createConcept(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "myConcept")
        interface NestedBuilder {
            @BuilderMethod
            fun setText(
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.TextFacet::class)
                textValue: String
            ): NestedSubBuilder
        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "myConcept")
        interface NestedSubBuilder {

            @BuilderMethod
            fun setNumber(
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.NumberFacet::class)
                numberValue: Int
            )
        }
    }

    @Test
    fun `test using the same alias in a sub-builder and a sub-sub-builder`() {
        val schemaInstance: SchemaWithConceptWithFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderUsingSameAliasForSameConceptInNestedBuilders::class) { builder ->
                builder
                    .createConcept()
                    .setText("myText")
                    .setNumber(17)
            }
        }
        assertEquals(1, schemaInstance.getConcepts().size)

        val myConcept = schemaInstance.getConcepts().first()
        assertEquals(17, myConcept.getNumber())
        assertEquals("myText", myConcept.getText())
    }

    @Builder
    private interface BuilderUsingSameAliasForTwoDifferentConceptsOnDifferentBuilderLevels {

        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue("myConcept")
        fun createConcept(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "myConcept")
        interface NestedBuilder {
            @BuilderMethod
            @SetFixedIntFacetValue(conceptToModifyAlias = "myConcept", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.NumberFacet::class, value = 42)
            fun setTextAndFixedNumber(
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.TextFacet::class)
                textValue: String
            ): NestedSubBuilder
        }

        @Builder
        // no ExpectedAliasFromSuperiorBuilder here, therefore "myConcept" is a new alias
        interface NestedSubBuilder {
            @BuilderMethod
            @NewConcept(concept = SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "myConcept")
            @SetRandomConceptIdentifierValue("myConcept")
            fun createConceptAndSetText(
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.TextFacet::class)
                textValue: String
            ): NestedSubSubBuilder
        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "myConcept")
        interface NestedSubSubBuilder {
            @BuilderMethod
            fun setNumber(
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.NumberFacet::class)
                numberValue: Int
            )
        }
    }

    @Test
    fun `test using the same alias in a sub-builder for a new concept as no ExpectedAliasFromSuperiorBuilder annotation is declared on the sub-builder`() {
        val schemaInstance: SchemaWithConceptWithFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderUsingSameAliasForTwoDifferentConceptsOnDifferentBuilderLevels::class) { builder ->
                builder
                    .createConcept().setTextAndFixedNumber("ConceptFromTopLevelBuilder")
                    .createConceptAndSetText("OtherConceptFromSubBuilder").setNumber(17)
            }
        }
        assertEquals(2, schemaInstance.getConcepts().size)

        val firstConcept = schemaInstance.getConcepts().first()

        assertEquals(42, firstConcept.getNumber())
        assertEquals("ConceptFromTopLevelBuilder", firstConcept.getText())

        val secondConcept = schemaInstance.getConcepts().last()
        assertEquals(17, secondConcept.getNumber())
        assertEquals("OtherConceptFromSubBuilder", secondConcept.getText())
    }
}