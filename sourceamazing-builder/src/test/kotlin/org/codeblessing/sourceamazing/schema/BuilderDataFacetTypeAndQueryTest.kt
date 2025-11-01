package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.BuilderDataFacetTypeAndQueryTest.SchemaWithConceptWithFacet.MyEnum
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.toConceptName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderDataFacetTypeAndQueryTest {

    private interface SchemaWithConceptWithFacet {

        enum class MyEnum {
            FOO,
            BAR,
        }

        interface ConceptWithFacet {
            @Facet
            val textFacetAsList: List<String>

            @Facet
            val boolFacetAsList: List<Boolean>

            @Facet
            val numberFacetAsList: List<Int>

            @Facet
            val enumerationFacetAsList: List<MyEnum>

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
            fun addFacetValues(
                @SetFacetValue(
                    facetToModify = "TextFacet",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myTextValue: String,
                @SetFacetValue(
                    facetToModify = "BoolFacet",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myBoolValue: Boolean,
                @SetFacetValue(
                    facetToModify = "NumberFacet",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myNumberValue: Int,
                @SetFacetValue(
                    facetToModify = "EnumerationFacet",
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
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderToAddOrReplaceFacets::class,
                    ) { builder ->
                        builder.createConcept()
                        // no facet values added
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(0, concept.textFacetAsList.size)
        Assertions.assertEquals(0, concept.boolFacetAsList.size)
        Assertions.assertEquals(0, concept.numberFacetAsList.size)
        Assertions.assertEquals(0, concept.enumerationFacetAsList.size)
    }

    @Test
    fun `test insert exactly one value for all the different types of facets does not fail and return null values`() {
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
        Assertions.assertEquals(1, concept.textFacetAsList.size)
        Assertions.assertEquals("hallo", concept.textFacetAsList[0])

        Assertions.assertEquals(1, concept.boolFacetAsList.size)
        Assertions.assertEquals(true, concept.boolFacetAsList[0])

        Assertions.assertEquals(1, concept.numberFacetAsList.size)
        Assertions.assertEquals(42, concept.numberFacetAsList[0])

        Assertions.assertEquals(1, concept.enumerationFacetAsList.size)
        Assertions.assertEquals(MyEnum.FOO, concept.enumerationFacetAsList[0])

    }

    @Test
    fun `test insert two values for all the different types of facets does not fail`() {
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
        Assertions.assertEquals(2, concept.textFacetAsList.size)
        Assertions.assertEquals("hallo1", concept.textFacetAsList[0])
        Assertions.assertEquals("hallo2", concept.textFacetAsList[1])

        Assertions.assertEquals(2, concept.boolFacetAsList.size)
        Assertions.assertEquals(false, concept.boolFacetAsList[0])
        Assertions.assertEquals(true, concept.boolFacetAsList[1])

        Assertions.assertEquals(2, concept.numberFacetAsList.size)
        Assertions.assertEquals(43, concept.numberFacetAsList[0])
        Assertions.assertEquals(44, concept.numberFacetAsList[1])

        Assertions.assertEquals(2, concept.enumerationFacetAsList.size)
        Assertions.assertEquals(MyEnum.BAR, concept.enumerationFacetAsList[0])
        Assertions.assertEquals(MyEnum.FOO, concept.enumerationFacetAsList[1])
    }

}
