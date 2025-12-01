package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SchemaDataFacetTypeAndQueryTest {

    private interface MyConcepts {

        enum class MyEnum {
            FOO,
            BAR,
        }

        interface MyConcept {
            val texts: List<String>

            val booleans: List<Boolean>

            val numbers: List<Int>

            val enumerations: List<MyEnum>
        }

        val concepts: List<MyConcept>
    }

    @Test
    fun `test insert zero values for all the different types of facets does not fail`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(0, concept.texts.size)
        Assertions.assertEquals(0, concept.booleans.size)
        Assertions.assertEquals(0, concept.numbers.size)
        Assertions.assertEquals(0, concept.enumerations.size)
    }

    @Test
    fun `test insert exactly one value for all the different types of facets does not fail and return null values`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        val concept = schemaInstance.concepts.first()
        Assertions.assertEquals(1, concept.texts.size)
        Assertions.assertEquals("hallo", concept.texts[0])

        Assertions.assertEquals(1, concept.booleans.size)
        Assertions.assertEquals(true, concept.booleans[0])

        Assertions.assertEquals(1, concept.numbers.size)
        Assertions.assertEquals(42, concept.numbers[0])

        Assertions.assertEquals(1, concept.enumerations.size)
        Assertions.assertEquals(MyConcepts.MyEnum.FOO, concept.enumerations[0])
    }

    @Test
    fun `test insert two values for all the different types of facets does not fail`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

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
        Assertions.assertEquals(MyConcepts.MyEnum.BAR, concept.enumerations[0])
        Assertions.assertEquals(MyConcepts.MyEnum.FOO, concept.enumerations[1])
    }
}
