package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.datacollection.addFacetValue
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.WrongTypeForFacetValueException
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Suppress("UNUSED")
class SchemaDataEnumTest {
    enum class AllDatatypesEnum {
        STRING,
        INT,
        FLOAT,
        DOUBLE,
        UUID,
    }

    private interface MyEnums {

        val enumFacetValue: AllDatatypesEnum
    }

    @Test
    fun `test using the enum type defined on the facet to set the enum value should not fail`() {
        val schemaInstance: MyEnums =
            SchemaApi.withSchema<MyEnums> { schemaContext ->
                schemaContext.withRootInstance<MyEnums> { root ->
                    root.addFacetValue(MyEnums::enumFacetValue, AllDatatypesEnum.INT)
                }
            }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.enumFacetValue)
    }

    enum class CompatibleNumericDatatypesEnum {
        INT,
        FLOAT,
        DOUBLE,
    }

    @Test
    fun `test using a enum type not defined on the facet but with subset of all enum values to set the enum value should not fail`() {
        val schemaInstance: MyEnums =
            SchemaApi.withSchema<MyEnums> { schemaContext ->
                schemaContext.withRootInstance<MyEnums> { root ->
                    root.addFacetValue(MyEnums::enumFacetValue, CompatibleNumericDatatypesEnum.INT)
                }
            }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.enumFacetValue)
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
        val schemaInstance: MyEnums =
            SchemaApi.withSchema<MyEnums> { schemaContext ->
                schemaContext.withRootInstance<MyEnums> { root ->
                    root.addFacetValue(MyEnums::enumFacetValue, ExactCopyOfAllDatatypesEnum.INT)
                }
            }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.enumFacetValue)
    }

    enum class IncompatibleWithNumericDatatypesEnum {
        INT,
        FLOAT,
        DOUBLE,
        BYTE, // BYTE is an incompatible facet value
    }

    @Test
    fun `test using a enum type from an incompatible subset of enum values but with a compatible enum type should not fail`() {
        SchemaApi.withSchema<MyEnums> { schemaContext ->
            schemaContext.withRootInstance<MyEnums> { root ->
                root.addFacetValue(MyEnums::enumFacetValue, IncompatibleWithNumericDatatypesEnum.INT)
            }
        }
    }

    @Test
    fun `test using a string instead of an enum value should not fail`() {
        SchemaApi.withSchema<MyEnums> { schemaContext ->
            schemaContext.withRootInstance<MyEnums> { root -> root.addFacetValue(MyEnums::enumFacetValue, "INT") }
        }
    }

    @Test
    fun `test using a enum type not defined on the facet and with a incompatible subset of enum values with an incompatible value should throw an exception`() {
        assertThrows<WrongTypeForFacetValueException> {
            SchemaApi.withSchema<MyEnums> { schemaContext ->
                schemaContext.withRootInstance<MyEnums> { root ->
                    root.addFacetValue(MyEnums::enumFacetValue, IncompatibleWithNumericDatatypesEnum.BYTE)
                }
            }
        }
    }
}
