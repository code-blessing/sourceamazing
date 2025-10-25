package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataNestingBuildersTest {

    private interface SchemaWithConceptWithFacet {

        interface ConceptWithFacet {
            @Facet
            val texts: List<String>

            @Facet
            val numbers: List<Int>

        }
        @Facet
        val concepts: List<ConceptWithFacet>
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
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "texts")
                textValue: String
            ): NestedSubBuilder
        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "myConcept")
        interface NestedSubBuilder {
            @BuilderMethod
            fun setNumber(
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "numbers")
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
        assertEquals(1, schemaInstance.concepts.size)

        val myConcepts = schemaInstance.concepts.first()

        assertEquals(listOf(17, 23), myConcepts.numbers)
        assertEquals(listOf("Added1", "Added2", "Added3"), myConcepts.texts)
    }
}
