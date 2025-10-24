package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.api.datacollection.newClazzModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class SchemaApiClazzModelIdTest {
    interface ReferenceClazz

    interface MyClazz {
        val id: String
    }

    interface MyRootClazzSchemaInterface {

        val myProperty: Set<MyClazz>
    }

    @Nested
    inner class ValueAsReference {

        @Test
        fun `test adding a value instead of a reference to the addClazzPropertyReference method should throw an exception`() {
            assertExceptionWithErrorCode<DataValidationException>(
                DataCollectionErrorCode.VALIDATION_FAILURES,
                DataCollectionErrorCode.MISSING_REFERENCED_CLAZZ_CLAZZ_PROPERTY_VALUE,
            ) {
                SchemaApi.withSchema(rootClazz = MyRootClazzSchemaInterface::class) { schemaContext ->
                    schemaContext.dataCollector.rootClazzModel().addClazzPropertyReference("myProperty", "a-value")
                }
            }
        }
    }

    @Nested
    inner class ClazzModelAsReference {

        @Test
        fun `test adding a single reference to the addClazzPropertyReference method not fail`() {
            val myRootClazz =
                SchemaApi.withSchema(rootClazz = MyRootClazzSchemaInterface::class) { schemaContext ->
                    val clazz1 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>().addClazzPropertyValue("id", "my-id-1")
                    val clazz2 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>().addClazzPropertyValue("id", "my-id-2")
                    schemaContext.dataCollector
                        .rootClazzModel()
                        .addClazzPropertyReference("myProperty", clazz1)
                        .addClazzPropertyReference("myProperty", clazz2)
                }

            assertEquals(listOf("my-id-1", "my-id-2"), myRootClazz.myProperty.map { it.id })
        }

        @Test
        fun `test adding a reference to the addClazzPropertyValue method instead of addClazzPropertyReference method should throw an exception`() {
            assertExceptionWithErrorCode<DataValidationException>(
                DataCollectionErrorCode.VALIDATION_FAILURES,
                DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_TYPE,
            ) {
                SchemaApi.withSchema(rootClazz = MyRootClazzSchemaInterface::class) { schemaContext ->
                    val clazz1 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>().addClazzPropertyValue("id", "my-id-1")
                    schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myProperty", clazz1)
                }
            }
        }

        @Test
        fun `test adding a list of references into the addClazzPropertyReference method should throw an exception`() {
            assertExceptionWithErrorCode<DataValidationException>(
                DataCollectionErrorCode.VALIDATION_FAILURES,
                DataCollectionErrorCode.MISSING_REFERENCED_CLAZZ_CLAZZ_PROPERTY_VALUE,
            ) {
                SchemaApi.withSchema(rootClazz = MyRootClazzSchemaInterface::class) { schemaContext ->
                    val clazz1 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>().addClazzPropertyValue("id", "my-id-1")
                    val clazz2 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>().addClazzPropertyValue("id", "my-id-2")

                    schemaContext.dataCollector
                        .rootClazzModel()
                        .addClazzPropertyReference("myProperty", listOf(clazz1, clazz2))
                }
            }
        }

        @Test
        fun `test adding a list of references into the addClazzPropertyReferences method should not fail`() {
            val myRootClazz =
                SchemaApi.withSchema(rootClazz = MyRootClazzSchemaInterface::class) { schemaContext ->
                    val clazz1 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>().addClazzPropertyValue("id", "my-id-1")
                    val clazz2 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>().addClazzPropertyValue("id", "my-id-2")

                    schemaContext.dataCollector
                        .rootClazzModel()
                        .addClazzPropertyReferences("myProperty", listOf(clazz1, clazz2))
                }

            assertEquals(listOf("my-id-1", "my-id-2"), myRootClazz.myProperty.map { it.id })
        }

        @Test
        fun `test adding a list of references into the replaceWithClazzPropertyReferences method should not fail`() {
            val myRootClazz =
                SchemaApi.withSchema(rootClazz = MyRootClazzSchemaInterface::class) { schemaContext ->
                    val clazz1 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>().addClazzPropertyValue("id", "my-id-1")
                    val clazz2 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>().addClazzPropertyValue("id", "my-id-2")

                    schemaContext.dataCollector
                        .rootClazzModel()
                        .replaceWithClazzPropertyReferences("myProperty", listOf(clazz1, clazz2))
                }

            assertEquals(listOf("my-id-1", "my-id-2"), myRootClazz.myProperty.map { it.id })
        }
    }

    @Nested
    inner class ClazzModelIdAsReference {

        @Test
        fun `test adding a single reference id to the addClazzPropertyReference method not fail`() {
            val myRootClazz =
                SchemaApi.withSchema(rootClazz = MyRootClazzSchemaInterface::class) { schemaContext ->
                    val clazzId1 =
                        schemaContext.dataCollector
                            .newClazzModel<MyClazz>()
                            .addClazzPropertyValue("id", "my-id-1")
                            .clazzAndModelId
                            .clazzModelId
                    val clazzId2 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>().addClazzPropertyValue("id", "my-id-2")
                    schemaContext.dataCollector
                        .rootClazzModel()
                        .addClazzPropertyReference("myProperty", clazzId1)
                        .addClazzPropertyReference("myProperty", clazzId2)
                }

            assertEquals(listOf("my-id-1", "my-id-2"), myRootClazz.myProperty.map { it.id })
        }

        @Test
        fun `test adding a reference to the addClazzPropertyValue method instead of addClazzPropertyReference method should throw an exception`() {
            assertExceptionWithErrorCode<DataValidationException>(
                DataCollectionErrorCode.VALIDATION_FAILURES,
                DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_TYPE,
            ) {
                SchemaApi.withSchema(rootClazz = MyRootClazzSchemaInterface::class) { schemaContext ->
                    val clazzId1 =
                        schemaContext.dataCollector
                            .newClazzModel<MyClazz>()
                            .addClazzPropertyValue("id", "my-id-1")
                            .clazzAndModelId
                            .clazzModelId
                    schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myProperty", clazzId1)
                }
            }
        }

        @Test
        fun `test adding a list of references into the addClazzPropertyReference method should throw an exception`() {
            assertExceptionWithErrorCode<DataValidationException>(
                DataCollectionErrorCode.VALIDATION_FAILURES,
                DataCollectionErrorCode.MISSING_REFERENCED_CLAZZ_CLAZZ_PROPERTY_VALUE,
            ) {
                SchemaApi.withSchema(rootClazz = MyRootClazzSchemaInterface::class) { schemaContext ->
                    val clazzId1 =
                        schemaContext.dataCollector
                            .newClazzModel<MyClazz>()
                            .addClazzPropertyValue("id", "my-id-1")
                            .clazzAndModelId
                            .clazzModelId
                    val clazzId2 =
                        schemaContext.dataCollector
                            .newClazzModel<MyClazz>()
                            .addClazzPropertyValue("id", "my-id-2")
                            .clazzAndModelId
                            .clazzModelId

                    schemaContext.dataCollector
                        .rootClazzModel()
                        .addClazzPropertyReference("myProperty", listOf(clazzId1, clazzId2))
                }
            }
        }

        @Test
        fun `test adding a list of references into the addClazzPropertyReferences method should not fail`() {
            val myRootClazz =
                SchemaApi.withSchema(rootClazz = MyRootClazzSchemaInterface::class) { schemaContext ->
                    val clazzId1 =
                        schemaContext.dataCollector
                            .newClazzModel<MyClazz>()
                            .addClazzPropertyValue("id", "my-id-1")
                            .clazzAndModelId
                            .clazzModelId
                    val clazzId2 =
                        schemaContext.dataCollector
                            .newClazzModel<MyClazz>()
                            .addClazzPropertyValue("id", "my-id-2")
                            .clazzAndModelId
                            .clazzModelId

                    schemaContext.dataCollector
                        .rootClazzModel()
                        .addClazzPropertyReferences("myProperty", listOf(clazzId1, clazzId2))
                }

            assertEquals(listOf("my-id-1", "my-id-2"), myRootClazz.myProperty.map { it.id })
        }

        @Test
        fun `test adding a list of references into the replaceWithClazzPropertyReferences method should not fail`() {
            val myRootClazz =
                SchemaApi.withSchema(rootClazz = MyRootClazzSchemaInterface::class) { schemaContext ->
                    val clazzId1 =
                        schemaContext.dataCollector
                            .newClazzModel<MyClazz>()
                            .addClazzPropertyValue("id", "my-id-1")
                            .clazzAndModelId
                            .clazzModelId
                    val clazzId2 =
                        schemaContext.dataCollector
                            .newClazzModel<MyClazz>()
                            .addClazzPropertyValue("id", "my-id-2")
                            .clazzAndModelId
                            .clazzModelId

                    schemaContext.dataCollector
                        .rootClazzModel()
                        .replaceWithClazzPropertyReferences("myProperty", listOf(clazzId1, clazzId2))
                }

            assertEquals(listOf("my-id-1", "my-id-2"), myRootClazz.myProperty.map { it.id })
        }
    }

    @Nested
    inner class ClazzModelIdEquality {

        @Test
        fun `test accessing the data collector with the id finds the correct clazz`() {
            val id1 = UniqueId.of("the-id-1")
            val id2 = UniqueId.of("the-id-2")
            val id1Alternative = UniqueId.of("the-id-1")
            val myRootClazz =
                SchemaApi.withSchema(rootClazz = MyRootClazzSchemaInterface::class) { schemaContext ->
                    val clazz1 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>(id1).addClazzPropertyValue("id", "my-id-1")
                    val clazz2 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>(id2).addClazzPropertyValue("id", "my-id-2")

                    schemaContext.dataCollector
                        .rootClazzModel()
                        .addClazzPropertyReferences("myProperty", listOf(clazz1, clazz2, clazz1))

                    schemaContext.dataCollector
                        .existingClazzModel(id1Alternative)
                        .replaceWithClazzPropertyValues("id", listOf("my-new-id-1"))
                }
            assertEquals(listOf("my-new-id-1", "my-id-2", "my-new-id-1"), myRootClazz.myProperty.map { it.id })
        }

        @Test
        fun `test accessing the data collector with very uncommon id finds the correct clazz`() {
            val id1: () -> Unit = {} // this is a very uncommon id
            val id2: (Int) -> Unit = {} // this is another very uncommon id
            val myRootClazz =
                SchemaApi.withSchema(rootClazz = MyRootClazzSchemaInterface::class) { schemaContext ->
                    val clazz1 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>(id1).addClazzPropertyValue("id", "my-id-1")
                    val clazz2 =
                        schemaContext.dataCollector.newClazzModel<MyClazz>(id2).addClazzPropertyValue("id", "my-id-2")

                    schemaContext.dataCollector
                        .rootClazzModel()
                        .addClazzPropertyReferences("myProperty", listOf(clazz1, clazz2, clazz1))

                    schemaContext.dataCollector
                        .existingClazzModel(id1)
                        .replaceWithClazzPropertyValues("id", listOf("my-new-id-1"))
                }
            assertEquals(listOf("my-new-id-1", "my-id-2", "my-new-id-1"), myRootClazz.myProperty.map { it.id })
        }
    }
}
