package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderDataAddOrReplaceNonNullAndNullValuesTest {

    private interface MyConcepts {

        interface MyConcept {
            val texts: List<String>
        }

        val concepts: List<MyConcept>
    }

    @Test
    fun `test insert to the same text facet multiple times with REPLACE mode does always clear and override the result`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept().setText("hallo1").setText("hallo2").setText("hallo3")
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(1, concept.texts.size)
        Assertions.assertEquals("hallo3", concept.texts[0])
    }

    @Test
    fun `test insert a list of strings to text facet with REPLACE mode does replace with all list entries`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept().setText("hallo1").setTexts(listOf("hallo2", "hallo3"))
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
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder
                            .createConcept()
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
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept().setText("hallo1").setTexts(emptyList())
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(0, concept.texts.size)
    }

    @Test
    fun `test insert null values to a text facet with REPLACE mode does not clear and override the result for null values`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder
                            .createConcept()
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
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept().addText("hallo1").addText("hallo2").addText("hallo3")
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
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept().addText("hallo1").addTexts(listOf("hallo2", "hallo3"))
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
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder
                            .createConcept()
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
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConcept().addText("hallo1").addTexts(emptyList())
                    }
                }
            }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(1, concept.texts.size)
        Assertions.assertEquals("hallo1", concept.texts[0])
    }

    @Test
    fun `test insert null values to the same text facet multiple times with ADD mode does not append the null values`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddOrReplaceFacets::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder
                            .createConcept()
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
