package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderDataCardinalityTest {

    private interface MyConcepts {

        interface MyConcept {

            val zeroToMultipleTexts: List<String>
        }

        val concepts: List<MyConcept>
    }

    @Test
    fun `test insert nothing to a text facet will return an empty list`() {
        val schemaInstance =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(0, concept.zeroToMultipleTexts.size)
    }

    @Test
    fun `test insert four texts individually to a text facet will return an list of four elements`() {
        val schemaInstance =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(4, concept.zeroToMultipleTexts.size)
    }

    @Test
    fun `test insert four texts as array list to a text facet will return an list of four elements`() {
        val schemaInstance =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(4, concept.zeroToMultipleTexts.size)
    }
}
