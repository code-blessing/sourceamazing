package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaDataAliasTest {

    private interface MyConcepts {

        interface MyConcept {
            val text: String

            val number: Int
        }

        val concepts: List<MyConcept>
    }

    @Test
    fun `test using the same alias in a sub-builder and a sub-sub-builder`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }
        assertEquals(1, schemaInstance.concepts.size)

        val myConcept = schemaInstance.concepts.first()
        assertEquals(17, myConcept.number)
        assertEquals("myText", myConcept.text)
    }

    @Test
    fun `test using the same alias in a sub-builder for a new concept as no ExpectedAliasFromSuperiorBuilder annotation is declared on the sub-builder`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }
        assertEquals(2, schemaInstance.concepts.size)

        val firstConcept = schemaInstance.concepts.first()

        assertEquals(42, firstConcept.number)
        assertEquals("ConceptFromTopLevelBuilder", firstConcept.text)

        val secondConcept = schemaInstance.concepts.last()
        assertEquals(17, secondConcept.number)
        assertEquals("OtherConceptFromSubBuilder", secondConcept.text)
    }
}
