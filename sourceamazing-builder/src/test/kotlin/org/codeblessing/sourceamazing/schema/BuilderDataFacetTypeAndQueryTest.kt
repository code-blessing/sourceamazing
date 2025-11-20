package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.BuilderDataFacetTypeAndQueryTest.SchemaWithConceptWithFacet.MyEnum
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderDataFacetTypeAndQueryTest {

    private interface SchemaWithConceptWithFacet {

        enum class MyEnum {
            FOO,
            BAR,
        }

        interface ConceptWithFacet {
            val texts: List<String>

            val booleans: List<Boolean>

            val numbers: List<Int>

            val enumerations: List<MyEnum>

        }

        val concepts: List<ConceptWithFacet>
    }


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderToAddOrReplaceFacets {

        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "myConcept")
        fun createConcept(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "myConcept")
        interface NestedBuilder {
            @BuilderMethod
            fun addFacetValues(
                @SetFacetValue(
                    facetToModify = "texts",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myTextValue: String,
                @SetFacetValue(
                    facetToModify = "booleans",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myBoolValue: Boolean,
                @SetFacetValue(
                    facetToModify = "numbers",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myNumberValue: Int,
                @SetFacetValue(
                    facetToModify = "enumerations",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myEnumValue: MyEnum,
            ): NestedBuilder
        }
    }

    @Test
    fun `test insert zero values for all the different types of facets does not fail`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { 
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                    ) { builder ->
                        builder.createConcept()
                        // no facet values added
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(0, concept.texts.size)
        Assertions.assertEquals(0, concept.booleans.size)
        Assertions.assertEquals(0, concept.numbers.size)
        Assertions.assertEquals(0, concept.enumerations.size)
    }

    @Test
    fun `test insert exactly one value for all the different types of facets does not fail and return null values`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { 
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                    ) { builder ->
                        builder.createConcept()
                            .addFacetValues(
                                myTextValue = "hallo",
                                myBoolValue = true,
                                myNumberValue = 42,
                                myEnumValue = MyEnum.FOO,
                            )
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(1, concept.texts.size)
        Assertions.assertEquals("hallo", concept.texts[0])

        Assertions.assertEquals(1, concept.booleans.size)
        Assertions.assertEquals(true, concept.booleans[0])

        Assertions.assertEquals(1, concept.numbers.size)
        Assertions.assertEquals(42, concept.numbers[0])

        Assertions.assertEquals(1, concept.enumerations.size)
        Assertions.assertEquals(MyEnum.FOO, concept.enumerations[0])

    }

    @Test
    fun `test insert two values for all the different types of facets does not fail`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { 
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                    ) { builder ->
                        builder.createConcept()
                            .addFacetValues(
                                myTextValue = "hallo1",
                                myBoolValue = false,
                                myNumberValue = 43,
                                myEnumValue = MyEnum.BAR,
                            )
                            .addFacetValues(
                                myTextValue = "hallo2",
                                myBoolValue = true,
                                myNumberValue = 44,
                                myEnumValue = MyEnum.FOO,
                            )
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(2, concept.texts.size)
        Assertions.assertEquals("hallo1", concept.texts[0])
        Assertions.assertEquals("hallo2", concept.texts[1])

        Assertions.assertEquals(2, concept.booleans.size)
        Assertions.assertEquals(false, concept.booleans[0])
        Assertions.assertEquals(true, concept.booleans[1])

        Assertions.assertEquals(2, concept.numbers.size)
        Assertions.assertEquals(43, concept.numbers[0])
        Assertions.assertEquals(44, concept.numbers[1])

        Assertions.assertEquals(2, concept.enumerations.size)
        Assertions.assertEquals(MyEnum.BAR, concept.enumerations[0])
        Assertions.assertEquals(MyEnum.FOO, concept.enumerations[1])
    }

}
