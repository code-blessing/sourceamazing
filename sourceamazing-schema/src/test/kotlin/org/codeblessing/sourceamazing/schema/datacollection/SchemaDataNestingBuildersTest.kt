package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaDataNestingBuildersTest {

    private interface MyConcepts {

        interface MyConcept {
            val texts: List<String>

            val numbers: List<Int>
        }

        val concepts: List<MyConcept>
    }

    @Test
    fun `test returning a higher level builder from a lower level builder`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }
        assertEquals(1, schemaInstance.concepts.size)

        val myConcepts = schemaInstance.concepts.first()

        assertEquals(listOf(17, 23), myConcepts.numbers)
        assertEquals(listOf("Added1", "Added2", "Added3"), myConcepts.texts)
    }
}
