package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

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

        val enumClazzPropertyValue: AllDatatypesEnum
    }

    @Test
    fun `test using the enum type defined on the clazzProperty to set the enum value should not fail`() {
        val schemaInstance: MyEnums =
            SchemaApi.withSchema<MyEnums> { schemaContext ->
                schemaContext.dataCollector
                    .rootClazzModel()
                    .addClazzPropertyValue("enumClazzPropertyValue", AllDatatypesEnum.INT)
            }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.enumClazzPropertyValue)
    }

    enum class CompatibleNumericDatatypesEnum {
        INT,
        FLOAT,
        DOUBLE,
    }

    @Test
    fun `test using a enum type not defined on the clazzProperty but with subset of all enum values to set the enum value should throw an exception`() {
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_ENUM_TYPE,
        ) {
            SchemaApi.withSchema<MyEnums> { schemaContext ->
                schemaContext.dataCollector
                    .rootClazzModel()
                    .addClazzPropertyValue("enumClazzPropertyValue", CompatibleNumericDatatypesEnum.INT)
            }
        }
    }

    enum class ExactCopyOfAllDatatypesEnum {
        STRING,
        INT,
        FLOAT,
        DOUBLE,
        UUID,
    }

    @Test
    fun `test using a enum type not defined on the clazzProperty but with exactly equal enum values to set the enum value should throw an exception`() {
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_ENUM_TYPE,
        ) {
            SchemaApi.withSchema<MyEnums> { schemaContext ->
                schemaContext.dataCollector
                    .rootClazzModel()
                    .addClazzPropertyValue("enumClazzPropertyValue", ExactCopyOfAllDatatypesEnum.INT)
            }
        }
    }

    enum class IncompatibleWithNumericDatatypesEnum {
        INT,
        FLOAT,
        DOUBLE,
        BYTE, // BYTE is an incompatible clazzProperty value
    }

    @Test
    fun `test using a enum type from an incompatible subset of enum values but with a compatible enum type should throw an exception`() {
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_ENUM_TYPE,
        ) {
            SchemaApi.withSchema<MyEnums> { schemaContext ->
                schemaContext.dataCollector
                    .rootClazzModel()
                    .addClazzPropertyValue("enumClazzPropertyValue", IncompatibleWithNumericDatatypesEnum.INT)
            }
        }
    }

    @Test
    fun `test using a string instead of an enum value should throw an exception`() {
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_ENUM_TYPE,
        ) {
            SchemaApi.withSchema<MyEnums> { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("enumClazzPropertyValue", "INT")
            }
        }
    }

    @Test
    fun `test using a enum type not defined on the clazzProperty and with a incompatible subset of enum values with an incompatible value should throw an exception`() {
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_ENUM_TYPE,
        ) {
            SchemaApi.withSchema<MyEnums> { schemaContext ->
                schemaContext.dataCollector
                    .rootClazzModel()
                    .addClazzPropertyValue("enumClazzPropertyValue", IncompatibleWithNumericDatatypesEnum.BYTE)
            }
        }
    }
}
