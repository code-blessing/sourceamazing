package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataInheritanceTypeTest {

    private interface MyConcepts {

        interface MyConcept {
            val texts: List<String>
        }

        val concepts: List<MyConcept>
    }

    @Test
    fun `test using a sub-builder declared as type parameter`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }
        assertEquals(1, schemaInstance.concepts.size)

        val myConcept = schemaInstance.concepts.first()
        assertEquals("myFirstText", myConcept.texts.first())
        assertEquals("mySecondText", myConcept.texts.last())
    }
}
