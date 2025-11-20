package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderDataAddOrReplaceNonNullAndNullValuesTest {

    private interface SchemaWithConceptWithFacet {

        interface ConceptWithFacet {
            val texts: List<String>

        }

        val concepts: List<ConceptWithFacet>
    }


    @Builder
    @ExpectedAliasFromSuperiorBuilder("root")
    private interface BuilderToAddOrReplaceFacets {

        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "concepts",
            referencedConceptAlias = "myConcept",
        )
        fun createConcept(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "myConcept")
        interface NestedBuilder {
            @BuilderMethod
            fun setText(
                @SetFacetValue(
                    facetToModify = "texts",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.REPLACE,
                )
                myValue: String,
            ): NestedBuilder

            @BuilderMethod
            fun setTextNullable(
                @SetFacetValue(
                    facetToModify = "texts",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.REPLACE,
                )
                @IgnoreNullFacetValue
                myNullableValue: String?,
            ): NestedBuilder

            @BuilderMethod
            fun setTexts(
                @SetFacetValue(
                    facetToModify = "texts",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.REPLACE,
                )
                myValues: List<String>,
            ): NestedBuilder

            @BuilderMethod
            fun setNullableTexts(
                @SetFacetValue(
                    facetToModify = "texts",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.REPLACE,
                )
                @IgnoreNullFacetValue
                myValues: List<String?>,
            ): NestedBuilder

            @BuilderMethod
            fun addText(
                @SetFacetValue(
                    facetToModify = "texts",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myValue: String,
            ): NestedBuilder

            @BuilderMethod
            fun addTextNullable(
                @SetFacetValue(
                    facetToModify = "texts",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                @IgnoreNullFacetValue
                myNullableValue: String?,
            ): NestedBuilder

            @BuilderMethod
            fun addTexts(
                @SetFacetValue(
                    facetToModify = "texts",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myValues: List<String>,
            ): NestedBuilder

            @BuilderMethod
            fun addNullableTexts(
                @SetFacetValue(
                    facetToModify = "texts",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                @IgnoreNullFacetValue
                myValues: List<String?>,
            ): NestedBuilder

        }
    }

    @Test
    fun `test insert to the same text facet multiple times with REPLACE mode does always clear and override the result`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept()
                            .setText("hallo1")
                            .setText("hallo2")
                            .setText("hallo3")
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(1, concept.texts.size)
        Assertions.assertEquals("hallo3", concept.texts[0])
    }

    @Test
    fun `test insert a list of strings to text facet with REPLACE mode does replace with all list entries`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept()
                            .setText("hallo1")
                            .setTexts(listOf("hallo2", "hallo3"))
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(2, concept.texts.size)
        Assertions.assertEquals("hallo2", concept.texts[0])
        Assertions.assertEquals("hallo3", concept.texts[1])
    }

    @Test
    fun `test insert a list of strings and null values to text facet with REPLACE mode does replace with all list entries that are not null`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept()
                            .setText("hallo1")
                            .setNullableTexts(listOf("hallo2", null, "hallo3", null))
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(2, concept.texts.size)
        Assertions.assertEquals("hallo2", concept.texts[0])
        Assertions.assertEquals("hallo3", concept.texts[1])
    }

    @Test
    fun `test insert an empty list of strings to text facet with REPLACE mode does replace with an empty list`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept()
                            .setText("hallo1")
                            .setTexts(emptyList())
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(0, concept.texts.size)
    }

    @Test
    fun `test insert null values to a text facet with REPLACE mode does not clear and override the result for null values`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept()
                            .setTextNullable("hallo1")
                            .setTextNullable(null)
                            .setText("hallo2")
                            .setTextNullable(null)
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(1, concept.texts.size)
        Assertions.assertEquals("hallo2", concept.texts[0])
    }

    @Test
    fun `test insert to the same text facet multiple times with ADD mode does append`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept()
                            .addText("hallo1")
                            .addText("hallo2")
                            .addText("hallo3")
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(3, concept.texts.size)
        Assertions.assertEquals("hallo1", concept.texts[0])
        Assertions.assertEquals("hallo2", concept.texts[1])
        Assertions.assertEquals("hallo3", concept.texts[2])
    }

    @Test
    fun `test insert a list of strings to text facet with ADD mode does append`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept()
                            .addText("hallo1")
                            .addTexts(listOf("hallo2", "hallo3"))
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(3, concept.texts.size)
        Assertions.assertEquals("hallo1", concept.texts[0])
        Assertions.assertEquals("hallo2", concept.texts[1])
        Assertions.assertEquals("hallo3", concept.texts[2])
    }

    @Test
    fun `test insert a list of strings and null values to text facet with ADD mode does append all non-null values`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept()
                            .addText("hallo1")
                            .addNullableTexts(listOf("hallo2", null, "hallo3", null))
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(3, concept.texts.size)
        Assertions.assertEquals("hallo1", concept.texts[0])
        Assertions.assertEquals("hallo2", concept.texts[1])
        Assertions.assertEquals("hallo3", concept.texts[2])
    }

    @Test
    fun `test insert an empty list of strings to text facet with ADD mode does not change the facet values`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept()
                            .addText("hallo1")
                            .addTexts(emptyList())
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(1, concept.texts.size)
        Assertions.assertEquals("hallo1", concept.texts[0])
    }

    @Test
    fun `test insert null values to the same text facet multiple times with ADD mode does not append the null values`() {
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept()
                            .addTextNullable("hallo1")
                            .addTextNullable(null)
                            .addText("hallo2")
                            .addTextNullable(null)
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(2, concept.texts.size)
        Assertions.assertEquals("hallo1", concept.texts[0])
        Assertions.assertEquals("hallo2", concept.texts[1])
    }
}
