package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataNestingBuildersTest {

    private interface MyClazzes {

        interface MyClazz {
            val texts: List<String>

            val numbers: List<Int>
        }

        val clazzes: List<MyClazz>
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderReturningASubBuilderInASubSubBuilder {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myClazz")
        fun createClazz(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        interface NestedBuilder {
            @BuilderMethod
            fun setText(@SetAsValue(alias = "myClazz", clazzProperty = "texts") textValue: String): NestedSubBuilder
        }

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        interface NestedSubBuilder {
            @BuilderMethod
            fun setNumber(@SetAsValue(alias = "myClazz", clazzProperty = "numbers") numberValue: Int): NestedBuilder
        }
    }

    @Test
    fun `test returning a higher level builder from a lower level builder`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderReturningASubBuilderInASubSubBuilder::class) { builder ->
                    builder
                        .createClazz()
                        .setText("Added1")
                        .setNumber(17)
                        .setText("Added2")
                        .setNumber(23)
                        .setText("Added3")
                }
            }
        assertEquals(1, schemaInstance.clazzes.size)

        val myClazzes = schemaInstance.clazzes.first()

        assertEquals(listOf(17, 23), myClazzes.numbers)
        assertEquals(listOf("Added1", "Added2", "Added3"), myClazzes.texts)
    }
}
