package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataNestingBuildersTest {

    private interface MyConcepts {

        interface MyConcept {
            val texts: List<String>

            val numbers: List<Int>
        }

        val concepts: List<MyConcept>
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts::class, conceptAlias = "root")
    private interface BuilderReturningASubBuilderInASubSubBuilder {

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
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts.MyConcept::class, conceptAlias = "myConcept")
        interface NestedBuilder {
            @BuilderMethod
            fun setText(
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "texts") textValue: String
            ): NestedSubBuilder
        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts.MyConcept::class, conceptAlias = "myConcept")
        interface NestedSubBuilder {
            @BuilderMethod
            fun setNumber(
                @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "numbers") numberValue: Int
            ): NestedBuilder
        }
    }

    @Test
    fun `test returning a higher level builder from a lower level builder`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderReturningASubBuilderInASubSubBuilder::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder
                            .createConcept()
                            .setText("Added1")
                            .setNumber(17)
                            .setText("Added2")
                            .setNumber(23)
                            .setText("Added3")
                    }
                }
            }
        assertEquals(1, schemaInstance.concepts.size)

        val myConcepts = schemaInstance.concepts.first()

        assertEquals(listOf(17, 23), myConcepts.numbers)
        assertEquals(listOf("Added1", "Added2", "Added3"), myConcepts.texts)
    }
}
