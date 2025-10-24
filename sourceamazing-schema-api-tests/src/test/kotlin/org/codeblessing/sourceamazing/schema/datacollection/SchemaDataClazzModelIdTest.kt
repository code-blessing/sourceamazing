package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.UniqueId
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.AdditionallyKnownClasses
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.api.datacollection.newClazzModel
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaDataClazzModelIdTest {

    private interface AbstractNumericClazz

    private interface ClazzOne : AbstractNumericClazz

    private interface ClazzTwo : AbstractNumericClazz

    @AdditionallyKnownClasses([ClazzOne::class, ClazzTwo::class])
    private interface MyClazzes {
        val clazzes: List<AbstractNumericClazz>
    }

    @Test
    fun `test using the different clazz identifier for creating same and different clazzes should not fail`() {
        val id1 = UniqueId.of("My-Id-1")
        val id2 = UniqueId.of("My-Id-2")
        val id3 = UniqueId.of("My-Id-3")

        val schemaInstance =
            SchemaApi.withSchema<MyClazzes> { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().let { rootClazz ->
                    schemaContext.dataCollector.newClazzModel<ClazzOne>(id1)
                    schemaContext.dataCollector.newClazzModel<ClazzTwo>(id2)
                    schemaContext.dataCollector.newClazzModel<ClazzOne>(id3)

                    rootClazz.addClazzPropertyReference("clazzes", id1)
                    rootClazz.addClazzPropertyReference("clazzes", id2)
                    rootClazz.addClazzPropertyReference("clazzes", id3)
                }
            }

        assertEquals(3, schemaInstance.clazzes.size)
    }

    @Test
    fun `test using the same clazz identifier for creating same clazzes throws an exception`() {
        val id = UniqueId.of("My-Id")
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.DUPLICATE_CLAZZ_IDENTIFIER,
        ) {
            SchemaApi.withSchema<MyClazzes> { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().let { rootClazz ->
                    schemaContext.dataCollector.newClazzModel<ClazzOne>(id)
                    schemaContext.dataCollector.newClazzModel<ClazzOne>(id)
                    rootClazz.addClazzPropertyReference("clazzes", id)
                }
            }
        }
    }

    @Test
    fun `test using the same clazz identifier for creating different clazzes throws an exception`() {
        val id = UniqueId.of("My-Id")
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.DUPLICATE_CLAZZ_IDENTIFIER,
        ) {
            SchemaApi.withSchema<MyClazzes> { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().let { rootClazz ->
                    schemaContext.dataCollector.newClazzModel<ClazzOne>(id)
                    schemaContext.dataCollector.newClazzModel<ClazzTwo>(id)
                    rootClazz.addClazzPropertyReference("clazzes", id)
                }
            }
        }
    }
}
