package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.UniqueId
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.api.datacollection.newClazzModel
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SchemaDataReferenceCircularDependenciesTest {

    private interface MyClazzesSchemaInterface {

        val clazzes: List<MyClazzSchemaInterface>
    }

    interface MyClazzSchemaInterface {
        val id: String

        val clazzReferences: List<MyClazzSchemaInterface>
    }

    @Nested
    inner class SchemaWithInterface {

        @Test
        fun `test add reference to itself`() {
            val selfReferencingId = UniqueId.of("Self-Referencing-Id")
            val schemaInstance: MyClazzesSchemaInterface =
                SchemaApi.withSchema<MyClazzesSchemaInterface> { schemaContext ->
                    schemaContext.dataCollector.rootClazzModel().let { root ->
                        val selfReferencingClazz =
                            schemaContext.dataCollector
                                .newClazzModel<MyClazzSchemaInterface>(selfReferencingId)
                                .addClazzPropertyValue("id", "self-referenced")

                        selfReferencingClazz.addClazzPropertyReference("clazzReferences", selfReferencingClazz)

                        root.addClazzPropertyReference("clazzes", selfReferencingClazz)
                    }
                }

            val selfReferencingClazz = schemaInstance.clazzes.first()
            assertEquals("self-referenced", selfReferencingClazz.id)
            assertEquals("self-referenced", selfReferencingClazz.clazzReferences.first().id)
        }

        @Test
        fun `test three clazz referencing each other in a circle`() {
            val firstId = UniqueId.of("First-Referenced-Id")
            val secondId = UniqueId.of("Second-Referenced-Id")
            val thirdId = UniqueId.of("Third-Referenced-Id")

            val schemaInstance: MyClazzesSchemaInterface =
                SchemaApi.withSchema<MyClazzesSchemaInterface> { schemaContext ->
                    schemaContext.dataCollector.rootClazzModel().let { root ->
                        val firstClazz =
                            schemaContext.dataCollector
                                .newClazzModel<MyClazzSchemaInterface>(firstId)
                                .addClazzPropertyValue("id", "first")

                        val secondClazz =
                            schemaContext.dataCollector
                                .newClazzModel<MyClazzSchemaInterface>(secondId)
                                .addClazzPropertyValue("id", "second")

                        val thirdClazz =
                            schemaContext.dataCollector
                                .newClazzModel<MyClazzSchemaInterface>(thirdId)
                                .addClazzPropertyValue("id", "third")

                        firstClazz.addClazzPropertyReference("clazzReferences", secondClazz)
                        secondClazz.addClazzPropertyReference("clazzReferences", thirdClazz)
                        thirdClazz.addClazzPropertyReference("clazzReferences", firstClazz)

                        root.addClazzPropertyReference("clazzes", firstClazz)
                    }
                }

            val theFirst = schemaInstance.clazzes.first()
            assertEquals("first", theFirst.id)
            val theSecond = theFirst.clazzReferences.first()
            assertEquals("second", theSecond.id)
            val theThird = theSecond.clazzReferences.first()
            assertEquals("third", theThird.id)
            val theFirstAgain = theThird.clazzReferences.first()
            assertEquals("first", theFirstAgain.id)
        }
    }

    data class MyClazzesSchemaClass(val clazzes: List<MyClazzSchemaClass>)

    data class MyClazzSchemaClass(val id: String, val clazzReferences: List<MyClazzSchemaClass>)

    @Nested
    inner class SchemaWithDataClass {

        @Test
        fun `test add reference to itself`() {
            val selfReferencingId = UniqueId.of("Self-Referencing-Id")
            assertExceptionWithErrorCode<DataValidationException>(
                DataCollectionErrorCode.UNRESOLVABLE_CIRCULAR_DEPENDENCY_DETECTED
            ) {
                SchemaApi.withSchema<MyClazzesSchemaClass> { schemaContext ->
                    schemaContext.dataCollector.rootClazzModel().let { root ->
                        val selfReferencingClazz =
                            schemaContext.dataCollector
                                .newClazzModel<MyClazzSchemaClass>(selfReferencingId)
                                .addClazzPropertyValue("id", "self-referenced")

                        selfReferencingClazz.addClazzPropertyReference("clazzReferences", selfReferencingClazz)

                        root.addClazzPropertyReference("clazzes", selfReferencingClazz)
                    }
                }
            }
        }

        @Test
        fun `test three clazz referencing each other in a circle`() {
            val firstId = UniqueId.of("First-Referenced-Id")
            val secondId = UniqueId.of("Second-Referenced-Id")
            val thirdId = UniqueId.of("Third-Referenced-Id")

            assertExceptionWithErrorCode<DataValidationException>(
                DataCollectionErrorCode.UNRESOLVABLE_CIRCULAR_DEPENDENCY_DETECTED
            ) {
                SchemaApi.withSchema<MyClazzesSchemaClass> { schemaContext ->
                    schemaContext.dataCollector.rootClazzModel().let { root ->
                        val firstClazz =
                            schemaContext.dataCollector
                                .newClazzModel<MyClazzSchemaClass>(firstId)
                                .addClazzPropertyValue("id", "first")

                        val secondClazz =
                            schemaContext.dataCollector
                                .newClazzModel<MyClazzSchemaClass>(secondId)
                                .addClazzPropertyValue("id", "second")

                        val thirdClazz =
                            schemaContext.dataCollector
                                .newClazzModel<MyClazzSchemaClass>(thirdId)
                                .addClazzPropertyValue("id", "third")

                        firstClazz.addClazzPropertyReference("clazzReferences", secondClazz)
                        secondClazz.addClazzPropertyReference("clazzReferences", thirdClazz)
                        thirdClazz.addClazzPropertyReference("clazzReferences", firstClazz)

                        root.addClazzPropertyReference("clazzes", firstClazz)
                    }
                }
            }
        }
    }
}
