package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderDataAddOrReplaceNonNullAndNullValuesTest {

    private interface MyClazzes {

        interface MyClazz {
            val texts: List<String>
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
            fun setText(
                @SetAsValue(
                    clazzProperty = "texts",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.REPLACE,
                )
                myValue: String
            ): NestedBuilder

            @BuilderMethod
            fun setTextNullable(
                @SetAsValue(
                    clazzProperty = "texts",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.REPLACE,
                )
                @IgnoreNullValue
                myNullableValue: String?
            ): NestedBuilder

            @BuilderMethod
            fun setTexts(
                @SetAsValue(
                    clazzProperty = "texts",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.REPLACE,
                )
                myValues: List<String>
            ): NestedBuilder

            @BuilderMethod
            fun setTextsVararg(
                @SetAsValue(
                    clazzProperty = "texts",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.REPLACE,
                )
                vararg myValues: String
            ): NestedBuilder

            @BuilderMethod
            fun setNullableTexts(
                @SetAsValue(
                    clazzProperty = "texts",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.REPLACE,
                )
                @IgnoreNullValue
                myValues: List<String?>
            ): NestedBuilder

            @BuilderMethod
            fun setNullableTextsVararg(
                @SetAsValue(
                    clazzProperty = "texts",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.REPLACE,
                )
                @IgnoreNullValue
                vararg myValues: String?
            ): NestedBuilder

            @BuilderMethod
            fun addText(
                @SetAsValue(clazzProperty = "texts", alias = "myClazz", modification = ClazzPropertyModification.ADD)
                myValue: String
            ): NestedBuilder

            @BuilderMethod
            fun addTextNullable(
                @SetAsValue(clazzProperty = "texts", alias = "myClazz", modification = ClazzPropertyModification.ADD)
                @IgnoreNullValue
                myNullableValue: String?
            ): NestedBuilder

            @BuilderMethod
            fun addTexts(
                @SetAsValue(clazzProperty = "texts", alias = "myClazz", modification = ClazzPropertyModification.ADD)
                myValues: List<String>
            ): NestedBuilder

            @BuilderMethod
            fun addTextsVararg(
                @SetAsValue(clazzProperty = "texts", alias = "myClazz", modification = ClazzPropertyModification.ADD)
                vararg myValues: String
            ): NestedBuilder

            @BuilderMethod
            fun addNullableTexts(
                @SetAsValue(clazzProperty = "texts", alias = "myClazz", modification = ClazzPropertyModification.ADD)
                @IgnoreNullValue
                myValues: List<String?>
            ): NestedBuilder

            @BuilderMethod
            fun addNullableTextsVararg(
                @SetAsValue(clazzProperty = "texts", alias = "myClazz", modification = ClazzPropertyModification.ADD)
                @IgnoreNullValue
                vararg myValues: String?
            ): NestedBuilder
        }
    }

    @Test
    fun `test insert to the same text clazzProperty multiple times with REPLACE mode does always clear and override the result`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder.createClazz().setText("hallo1").setText("hallo2").setText("hallo3")
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(1, clazz.texts.size)
        Assertions.assertEquals("hallo3", clazz.texts[0])
    }

    @Test
    fun `test insert a list of strings to text clazzProperty with REPLACE mode does replace with all list entries`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder.createClazz().setTextsVararg("hallo1").setTexts(listOf("hallo2", "hallo3"))
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(2, clazz.texts.size)
        Assertions.assertEquals("hallo2", clazz.texts[0])
        Assertions.assertEquals("hallo3", clazz.texts[1])
    }

    @Test
    fun `test insert a list of strings and null values to text clazzProperty with REPLACE mode does replace with all list entries that are not null`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder
                        .createClazz()
                        .setText("hallo1")
                        .setNullableTexts(listOf("hallo2", null, "hallo3", null))
                        .setNullableTextsVararg("hallo4", null, "hallo5", null)
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(2, clazz.texts.size)
        Assertions.assertEquals("hallo4", clazz.texts[0])
        Assertions.assertEquals("hallo5", clazz.texts[1])
    }

    @Test
    fun `test insert an empty list of strings to text clazzProperty with REPLACE mode does replace with an empty list`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder.createClazz().setText("hallo1").setTexts(emptyList()).setTextsVararg()
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(0, clazz.texts.size)
    }

    @Test
    fun `test insert null values to a text clazzProperty with REPLACE mode does not clear and override the result for null values`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder
                        .createClazz()
                        .setTextNullable("hallo1")
                        .setTextNullable(null)
                        .setText("hallo2")
                        .setTextNullable(null)
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(1, clazz.texts.size)
        Assertions.assertEquals("hallo2", clazz.texts[0])
    }

    @Test
    fun `test insert to the same text clazzProperty multiple times with ADD mode does append`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder.createClazz().addText("hallo1").addText("hallo2").addText("hallo3")
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(3, clazz.texts.size)
        Assertions.assertEquals("hallo1", clazz.texts[0])
        Assertions.assertEquals("hallo2", clazz.texts[1])
        Assertions.assertEquals("hallo3", clazz.texts[2])
    }

    @Test
    fun `test insert a list of strings to text clazzProperty with ADD mode does append`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder.createClazz().addTextsVararg("hallo1").addTexts(listOf("hallo2", "hallo3"))
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(3, clazz.texts.size)
        Assertions.assertEquals("hallo1", clazz.texts[0])
        Assertions.assertEquals("hallo2", clazz.texts[1])
        Assertions.assertEquals("hallo3", clazz.texts[2])
    }

    @Test
    fun `test insert a list of strings and null values to text clazzProperty with ADD mode does append all non-null values`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder
                        .createClazz()
                        .addNullableTextsVararg("hallo1", null, "hallo2")
                        .addNullableTexts(listOf("hallo3", null, "hallo4", null))
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(4, clazz.texts.size)
        Assertions.assertEquals("hallo1", clazz.texts[0])
        Assertions.assertEquals("hallo2", clazz.texts[1])
        Assertions.assertEquals("hallo3", clazz.texts[2])
        Assertions.assertEquals("hallo4", clazz.texts[3])
    }

    @Test
    fun `test insert an empty list of strings to text clazzProperty with ADD mode does not change the clazzProperty values`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder.createClazz().addText("hallo1").addTexts(emptyList()).addTextsVararg()
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(1, clazz.texts.size)
        Assertions.assertEquals("hallo1", clazz.texts[0])
    }

    @Test
    fun `test insert null values to the same text clazzProperty multiple times with ADD mode does not append the null values`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder
                        .createClazz()
                        .addTextNullable("hallo1")
                        .addTextNullable(null)
                        .addText("hallo2")
                        .addTextNullable(null)
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(2, clazz.texts.size)
        Assertions.assertEquals("hallo1", clazz.texts[0])
        Assertions.assertEquals("hallo2", clazz.texts[1])
    }
}
