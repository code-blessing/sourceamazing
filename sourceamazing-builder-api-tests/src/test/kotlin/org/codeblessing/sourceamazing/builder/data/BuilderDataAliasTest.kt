package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataAliasTest {

    private interface MyClazzes {

        interface MyClazz {
            val text: String

            val number: Int
        }

        val clazzes: List<MyClazz>
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderUsingSameAliasForSameClazzInNestedBuilders {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myClazz")
        fun createClazz(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        interface NestedBuilder {
            @BuilderMethod
            fun setText(@SetAsValue(alias = "myClazz", clazzProperty = "text") textValue: String): NestedSubBuilder
        }

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        interface NestedSubBuilder {

            @BuilderMethod fun setNumber(@SetAsValue(alias = "myClazz", clazzProperty = "number") numberValue: Int)
        }
    }

    @Test
    fun `test using the same alias in a sub-builder and a sub-sub-builder`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderUsingSameAliasForSameClazzInNestedBuilders::class) {
                    builder ->
                    builder.createClazz().setText("myText").setNumber(17)
                }
            }
        assertEquals(1, schemaInstance.clazzes.size)

        val myClazz = schemaInstance.clazzes.first()
        assertEquals(17, myClazz.number)
        assertEquals("myText", myClazz.text)
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderUsingSameAliasForTwoDifferentClazzesOnDifferentBuilderLevels {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myClazz")
        fun createClazz(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        interface NestedBuilder {
            @BuilderMethod
            @SetFixedIntValue(alias = "myClazz", clazzProperty = "number", value = 42)
            fun setTextAndFixedNumber(
                @SetAsValue(alias = "myClazz", clazzProperty = "text") textValue: String
            ): NestedSubBuilder
        }

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
        // no ExpectedClazzModelFromSuperiorBuilder("myClazz) here, therefore "myClazz" is a new
        // alias
        interface NestedSubBuilder {
            @BuilderMethod
            @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
            @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myClazz")
            fun createClazzAndSetText(
                @SetAsValue(alias = "myClazz", clazzProperty = "text") textValue: String
            ): NestedSubSubBuilder
        }

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        interface NestedSubSubBuilder {
            @BuilderMethod fun setNumber(@SetAsValue(alias = "myClazz", clazzProperty = "number") numberValue: Int)
        }
    }

    @Test
    fun `test using the same alias in a sub-builder for a new clazz as no ExpectedAliasFromSuperiorBuilder annotation is declared on the sub-builder`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderUsingSameAliasForTwoDifferentClazzesOnDifferentBuilderLevels::class,
                ) { builder ->
                    builder
                        .createClazz()
                        .setTextAndFixedNumber("ClazzFromTopLevelBuilder")
                        .createClazzAndSetText("OtherClazzFromSubBuilder")
                        .setNumber(17)
                }
            }
        assertEquals(2, schemaInstance.clazzes.size)

        val firstClazz = schemaInstance.clazzes.first()

        assertEquals(42, firstClazz.number)
        assertEquals("ClazzFromTopLevelBuilder", firstClazz.text)

        val secondClazz = schemaInstance.clazzes.last()
        assertEquals(17, secondClazz.number)
        assertEquals("OtherClazzFromSubBuilder", secondClazz.text)
    }
}
