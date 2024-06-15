package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
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

class BuilderDataNestingBuildersTest {

    @Schema(concepts = [
        SchemaWithConceptWithFacet.ConceptWithFacet::class,
    ])
    private interface SchemaWithConceptWithFacet {

        @Concept(facets = [
            ConceptWithFacet.TextFacet::class,
            ConceptWithFacet.NumberFacet::class,
        ])
        interface ConceptWithFacet {

            @StringFacet(maximumOccurrences = 10)
            interface TextFacet

            @IntFacet(maximumOccurrences = 10)
            interface NumberFacet


            @QueryFacetValue(TextFacet::class)
            fun getTexts(): List<String>

            @QueryFacetValue(NumberFacet::class)
            fun getNumbers(): List<Int>

        }
        @QueryConcepts(conceptClasses = [ConceptWithFacet::class])
        fun getConcepts(): List<ConceptWithFacet>
    }

    @Builder
    private interface BuilderReturningASubBuilderInASubSubBuilder {

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
            ): NestedBuilder
        }
    }

    @Test
    fun `test returning a higher level builder from a lower level builder`() {
        val schemaInstance: SchemaWithConceptWithFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderReturningASubBuilderInASubSubBuilder::class) { builder ->
                builder
                    .createConcept()
                    .setText("Added1")
                    .setNumber(17)
                    .setText("Added2")
                    .setNumber(23)
                    .setText("Added3")
            }
        }
        assertEquals(1, schemaInstance.getConcepts().size)

        val myConcepts = schemaInstance.getConcepts().first()

        assertEquals(listOf(17, 23), myConcepts.getNumbers())
        assertEquals(listOf("Added1", "Added2", "Added3"), myConcepts.getTexts())
    }
}