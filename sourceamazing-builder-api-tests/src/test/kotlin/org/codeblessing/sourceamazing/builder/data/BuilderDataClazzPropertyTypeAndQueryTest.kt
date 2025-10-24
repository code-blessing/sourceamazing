package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderDataClazzPropertyTypeAndQueryTest {

    private interface MyClasses {

        enum class MyEnum {
            FOO,
            BAR,
        }

        interface MyClazz {
            val texts: List<String>

            val booleans: List<Boolean>

            val numbers: List<Int>

            val enumerations: List<MyEnum>
        }

        val clazzes: List<MyClazz>
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClasses::class, alias = "root")
    private interface BuilderToAddOrReplaceClazzProperties {

        @BuilderMethod
        @NewClazzModel(clazz = MyClasses.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myClazz")
        fun createClazz(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClasses.MyClazz::class, alias = "myClazz")
        interface NestedBuilder {
            @BuilderMethod
            fun addClazzPropertyValues(
                @SetAsValue(clazzProperty = "texts", alias = "myClazz", modification = ClazzPropertyModification.ADD)
                myTextValue: String,
                @SetAsValue(clazzProperty = "booleans", alias = "myClazz", modification = ClazzPropertyModification.ADD)
                myBoolValue: Boolean,
                @SetAsValue(clazzProperty = "numbers", alias = "myClazz", modification = ClazzPropertyModification.ADD)
                myNumberValue: Int,
                @SetAsValue(
                    clazzProperty = "enumerations",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.ADD,
                )
                myEnumValue: MyClasses.MyEnum,
            ): NestedBuilder
        }
    }

    @Test
    fun `test insert zero values for all the different types of clazzProperties does not fail`() {
        val schemaInstance: MyClasses =
            SchemaApi.withSchema(MyClasses::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder.createClazz()
                    // no clazzProperty values added
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(0, clazz.texts.size)
        Assertions.assertEquals(0, clazz.booleans.size)
        Assertions.assertEquals(0, clazz.numbers.size)
        Assertions.assertEquals(0, clazz.enumerations.size)
    }

    @Test
    fun `test insert exactly one value for all the different types of clazzProperties does not fail and return null values`() {
        val schemaInstance: MyClasses =
            SchemaApi.withSchema(MyClasses::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder
                        .createClazz()
                        .addClazzPropertyValues(
                            myTextValue = "hallo",
                            myBoolValue = true,
                            myNumberValue = 42,
                            myEnumValue = MyClasses.MyEnum.FOO,
                        )
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(1, clazz.texts.size)
        Assertions.assertEquals("hallo", clazz.texts[0])

        Assertions.assertEquals(1, clazz.booleans.size)
        Assertions.assertEquals(true, clazz.booleans[0])

        Assertions.assertEquals(1, clazz.numbers.size)
        Assertions.assertEquals(42, clazz.numbers[0])

        Assertions.assertEquals(1, clazz.enumerations.size)
        Assertions.assertEquals(MyClasses.MyEnum.FOO, clazz.enumerations[0])
    }

    @Test
    fun `test insert two values for all the different types of clazzProperties does not fail`() {
        val schemaInstance: MyClasses =
            SchemaApi.withSchema(MyClasses::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzProperties::class) { builder ->
                    builder
                        .createClazz()
                        .addClazzPropertyValues(
                            myTextValue = "hallo1",
                            myBoolValue = false,
                            myNumberValue = 43,
                            myEnumValue = MyClasses.MyEnum.BAR,
                        )
                        .addClazzPropertyValues(
                            myTextValue = "hallo2",
                            myBoolValue = true,
                            myNumberValue = 44,
                            myEnumValue = MyClasses.MyEnum.FOO,
                        )
                }
            }

        val clazz = schemaInstance.clazzes.first()
        Assertions.assertEquals(2, clazz.texts.size)
        Assertions.assertEquals("hallo1", clazz.texts[0])
        Assertions.assertEquals("hallo2", clazz.texts[1])

        Assertions.assertEquals(2, clazz.booleans.size)
        Assertions.assertEquals(false, clazz.booleans[0])
        Assertions.assertEquals(true, clazz.booleans[1])

        Assertions.assertEquals(2, clazz.numbers.size)
        Assertions.assertEquals(43, clazz.numbers[0])
        Assertions.assertEquals(44, clazz.numbers[1])

        Assertions.assertEquals(2, clazz.enumerations.size)
        Assertions.assertEquals(MyClasses.MyEnum.BAR, clazz.enumerations[0])
        Assertions.assertEquals(MyClasses.MyEnum.FOO, clazz.enumerations[1])
    }
}
