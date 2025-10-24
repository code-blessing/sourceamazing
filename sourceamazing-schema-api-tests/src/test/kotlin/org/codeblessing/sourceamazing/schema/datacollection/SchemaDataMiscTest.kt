package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class SchemaDataMiscTest {
    private interface MyTextClazzes {
        val myText: String
    }

    @Test
    fun `test insert a list of strings as clazzProperty value to the non-list clazzProperty method should throw an exception`() {
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema<MyTextClazzes> { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().let { clazzData ->
                    clazzData.addClazzPropertyValue("myText", listOf("hallo1"))
                    clazzData.addClazzPropertyValue("myText", listOf("hallo1"))
                }
            }
        }
    }

    enum class MyEnum {
        A,
        B,
        C,
    }

    private interface MyEnumClazzes {
        val myEnum: MyEnum
    }

    @Test
    fun `test insert a list as clazzProperty value to the non-list clazzProperty method should throw an exception`() {
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_ENUM_TYPE,
        ) {
            SchemaApi.withSchema<MyEnumClazzes> { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myEnum", listOf(MyEnum.A))
            }
        }
    }

    private interface MyMixedClazzes {
        val myMixedClazzes: List<MixedClazz>
    }

    sealed interface MixedClazz {
        val id: String
    }

    enum class EnumMix(val enumId: String) : MixedClazz {
        A("EnumMixA") {
            override val id: String
                get() = enumId
        },
        B("EnumMixB"),
        C("EnumMixC");

        override val id: String = enumId
    }

    data class DataMix(override val id: String) : MixedClazz

    interface SubInterfaceMix : MixedClazz

    @Test
    fun `test mixed kind of classes in one clazzProperty should not fail`() {
        val schemaInstance =
            SchemaApi.withSchema<MyMixedClazzes> { schemaContext ->
                val subInterfaceMix =
                    schemaContext.dataCollector
                        .newClazzModel(SubInterfaceMix::class)
                        .addClazzPropertyValue("id", "SubInterfaceMix")

                schemaContext.dataCollector
                    .rootClazzModel()
                    .addClazzPropertyValue("myMixedClazzes", EnumMix.A)
                    .addClazzPropertyValue("myMixedClazzes", EnumMix.B)
                    .addClazzPropertyValue("myMixedClazzes", DataMix("DataMix"))
                    .addClazzPropertyReference("myMixedClazzes", subInterfaceMix)
            }

        Assertions.assertEquals(
            listOf("EnumMixA", "EnumMixB", "DataMix", "SubInterfaceMix"),
            schemaInstance.myMixedClazzes.map { it.id },
        )
    }

    private interface MyAnyClazzes {
        val myProvidedTypes: List<Any>
    }

    @Test
    fun `test Any data class as clazzProperty`() {
        SchemaApi.withSchema<MyAnyClazzes> { schemaContext ->
            schemaContext.dataCollector.rootClazzModel().let { clazzData ->
                val anyModel = schemaContext.dataCollector.newClazzModel(Any::class)

                clazzData.addClazzPropertyValue("myProvidedTypes", "hallo1")
                clazzData.addClazzPropertyReference("myProvidedTypes", anyModel)
            }
        }
    }
}
