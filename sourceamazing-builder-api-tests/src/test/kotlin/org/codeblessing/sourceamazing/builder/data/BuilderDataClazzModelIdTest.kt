package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.data.BuilderDataClazzModelIdTest.MyClazzes.ClazzOne
import org.codeblessing.sourceamazing.builder.data.BuilderDataClazzModelIdTest.MyClazzes.ClazzTwo
import org.codeblessing.sourceamazing.schema.UniqueId
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.AdditionallyKnownClasses
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataClazzModelIdTest {

    @AdditionallyKnownClasses([ClazzOne::class, ClazzTwo::class])
    private interface MyClazzes {

        interface AbstractNumericClazz

        interface ClazzOne : AbstractNumericClazz

        interface ClazzTwo : AbstractNumericClazz

        val clazzes: List<AbstractNumericClazz>
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderToAddClazzes {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.ClazzOne::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myClazz")
        fun createClazzOne(@SetAsClazzModelId(alias = "myClazz") clazzModelId: UniqueId)

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.ClazzTwo::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myClazz")
        fun createClazzTwo(@SetAsClazzModelId(alias = "myClazz") clazzModelId: UniqueId)
    }

    @Test
    fun `test using the different clazz identifier for creating same and different clazzes should not fail`() {
        val myClazzModelId1 = UniqueId.of("My-Id-1")
        val myClazzModelId2 = UniqueId.of("My-Id-2")
        val myClazzModelId3 = UniqueId.of("My-Id-3")

        val schemaInstance =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddClazzes::class) { builder ->
                    builder.createClazzOne(myClazzModelId1)
                    builder.createClazzOne(myClazzModelId2)
                    builder.createClazzTwo(myClazzModelId3)
                }
            }

        assertEquals(3, schemaInstance.clazzes.size)
    }

    @Test
    fun `test using the same clazz identifier for creating same clazzes throws an exception`() {
        val myClazzModelId = UniqueId.of("My-Id")
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.DUPLICATE_CLAZZ_IDENTIFIER,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddClazzes::class) { builder ->
                    builder.createClazzOne(myClazzModelId)
                    builder.createClazzOne(myClazzModelId)
                }
            }
        }
    }

    @Test
    fun `test using the same clazz identifier for creating different clazzes throws an exception`() {
        val myClazzModelId = UniqueId.of("My-Id")

        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.DUPLICATE_CLAZZ_IDENTIFIER,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddClazzes::class) { builder ->
                    builder.createClazzOne(myClazzModelId)
                    builder.createClazzTwo(myClazzModelId)
                }
            }
        }
    }

    @Test
    fun `test quick validation throws the exception immediately on the wrong builder method`() {
        val myClazzModelId = UniqueId.of("My-Id")

        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderToAddClazzes::class) { builder ->
                builder.createClazzOne(myClazzModelId)
                assertExceptionWithErrorCode<DataValidationException>(
                    DataCollectionErrorCode.VALIDATION_FAILURES,
                    DataCollectionErrorCode.DUPLICATE_CLAZZ_IDENTIFIER,
                ) {
                    builder.createClazzTwo(myClazzModelId)
                }
            }
        }
    }
}
