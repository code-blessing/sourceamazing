package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderDataCardinalityTest {

    private interface MyClazzes {

        interface MyClazz {

            val zeroToMultipleTexts: List<String>
        }

        val clazzes: List<MyClazz>
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderToAddOrReplaceClazzProperties {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myClazz")
        fun createClazz(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        interface NestedBuilder {
            @BuilderMethod
            fun addText(
                @SetAsValue(
                    clazzProperty = "zeroToMultipleTexts",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.ADD,
                )
                myValue: String
            ): NestedBuilder

            @BuilderMethod
            fun addTexts(
                @SetAsValue(
                    clazzProperty = "zeroToMultipleTexts",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.ADD,
                )
                vararg myValues: String
            ): NestedBuilder
        }
    }

    @Test
    fun `test insert nothing to a text clazzProperty will return an empty list`() {
        val schemaInstance =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder.createClazz()
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(0, clazz.zeroToMultipleTexts.size)
    }

    @Test
    fun `test insert four texts individually to a text clazzProperty will return an list of four elements`() {
        val schemaInstance =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder.createClazz().addText("hallo1").addText("hallo2").addText("hallo3").addText("hallo4")
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(4, clazz.zeroToMultipleTexts.size)
    }

    @Test
    fun `test insert four texts as array list to a text clazzProperty will return an list of four elements`() {
        val schemaInstance =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder.createClazz().addTexts("hallo1", "hello2", "hallo3", "hallo4")
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(4, clazz.zeroToMultipleTexts.size)
    }
}
