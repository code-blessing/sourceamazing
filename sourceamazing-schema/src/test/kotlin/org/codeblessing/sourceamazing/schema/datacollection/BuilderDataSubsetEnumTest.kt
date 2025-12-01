package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Suppress("UNUSED")
class BuilderDataSubsetEnumTest {
    enum class AllDatatypesEnum {
        STRING,
        INT,
        FLOAT,
        DOUBLE,
        UUID,
    }

    private interface MyConcepts {

        interface MyConcept {

            val enumFacetValue: AllDatatypesEnum
        }

        val concept: MyConcept
    }

    @Test
    fun `test using the enum type defined on the facet to set the enum value should not fail`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.concept.enumFacetValue)
    }

    enum class CompatibleNumericDatatypesEnum {
        INT,
        FLOAT,
        DOUBLE,
    }

    @Test
    fun `test using a enum type not defined on the facet but with subset of all enum values to set the enum value should not fail`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.concept.enumFacetValue)
    }

    enum class ExactCopyOfAllDatatypesEnum {
        STRING,
        INT,
        FLOAT,
        DOUBLE,
        UUID,
    }

    @Test
    fun `test using a enum type not defined on the facet but with exactly equal enum values to set the enum value should not fail`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.concept.enumFacetValue)
    }

    enum class IncompatibleWithNumericDatatypesEnum {
        INT,
        FLOAT,
        DOUBLE,
        BYTE, // this is an incompatible facet value
    }

    @Test
    fun `test using a enum type not defined on the facet but with a incompatible subset of enum values to set the enum value should throw an exception`() {
        assertThrows<SyntaxException> {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext -> TODO("implement test setup") }
        }
    }
}
