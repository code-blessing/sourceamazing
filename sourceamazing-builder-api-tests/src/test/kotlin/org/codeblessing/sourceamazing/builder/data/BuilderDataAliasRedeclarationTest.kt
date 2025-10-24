package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataAliasRedeclarationTest {

    private interface MyClazzes {

        interface MyClazz {
            val text: String
        }

        val clazzes: List<MyClazz>
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    interface BuilderUsingSameAliasForAnotherClazz {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myClazz")
        fun createClazz(): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
        interface NestedBuilder {
            @BuilderMethod
            fun setText(@SetAsValue(alias = "myClazz", clazzProperty = "text") textValue: String): NestedBuilder

            @BuilderMethod
            @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "myOtherClazz")
            @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myOtherClazz")
            @RedeclareAliasForNestedBuilder(alias = "myOtherClazz", newAlias = "myClazz")
            fun createNewClazz(): NestedBuilder
        }
    }

    @Test
    fun `test using the same alias in a sub-builder and a sub-sub-builder`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderUsingSameAliasForAnotherClazz::class) { builder ->
                    builder
                        .createClazz()
                        .setText("myClazzA")
                        .createNewClazz()
                        .setText("myClazzB")
                        .createNewClazz()
                        .setText("myClazzC")
                }
            }
        assertEquals(3, schemaInstance.clazzes.size)

        assertEquals("myClazzA", schemaInstance.clazzes[0].text)
        assertEquals("myClazzB", schemaInstance.clazzes[1].text)
        assertEquals("myClazzC", schemaInstance.clazzes[2].text)
    }
}
