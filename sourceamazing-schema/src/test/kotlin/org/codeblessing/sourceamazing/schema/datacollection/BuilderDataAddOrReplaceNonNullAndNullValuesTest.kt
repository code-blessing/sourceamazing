package org.codeblessing.sourceamazing.schema.datacollection

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
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(1, concept.texts.size)
        Assertions.assertEquals("hallo3", concept.texts[0])
    }

    @Test
    fun `test insert a list of strings to text facet with REPLACE mode does replace with all list entries`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(2, concept.texts.size)
        Assertions.assertEquals("hallo2", concept.texts[0])
        Assertions.assertEquals("hallo3", concept.texts[1])
    }

    @Test
    fun `test insert a list of strings and null values to text facet with REPLACE mode does replace with all list entries that are not null`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(2, concept.texts.size)
        Assertions.assertEquals("hallo2", concept.texts[0])
        Assertions.assertEquals("hallo3", concept.texts[1])
    }

    @Test
    fun `test insert an empty list of strings to text facet with REPLACE mode does replace with an empty list`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(0, concept.texts.size)
    }

    @Test
    fun `test insert null values to a text facet with REPLACE mode does not clear and override the result for null values`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(1, concept.texts.size)
        Assertions.assertEquals("hallo2", concept.texts[0])
    }

    @Test
    fun `test insert to the same text facet multiple times with ADD mode does append`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(3, concept.texts.size)
        Assertions.assertEquals("hallo1", concept.texts[0])
        Assertions.assertEquals("hallo2", concept.texts[1])
        Assertions.assertEquals("hallo3", concept.texts[2])
    }

    @Test
    fun `test insert a list of strings to text facet with ADD mode does append`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(3, concept.texts.size)
        Assertions.assertEquals("hallo1", concept.texts[0])
        Assertions.assertEquals("hallo2", concept.texts[1])
        Assertions.assertEquals("hallo3", concept.texts[2])
    }

    @Test
    fun `test insert a list of strings and null values to text facet with ADD mode does append all non-null values`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(3, concept.texts.size)
        Assertions.assertEquals("hallo1", concept.texts[0])
        Assertions.assertEquals("hallo2", concept.texts[1])
        Assertions.assertEquals("hallo3", concept.texts[2])
    }

    @Test
    fun `test insert an empty list of strings to text facet with ADD mode does not change the facet values`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(1, concept.texts.size)
        Assertions.assertEquals("hallo1", concept.texts[0])
    }

    @Test
    fun `test insert null values to the same text facet multiple times with ADD mode does not append the null values`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(2, concept.texts.size)
        Assertions.assertEquals("hallo1", concept.texts[0])
        Assertions.assertEquals("hallo2", concept.texts[1])
    }
}
