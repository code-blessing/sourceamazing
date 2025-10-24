package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataDefaultMethodTest {

    private interface MyClazzes {

        interface MyClazz {
            val text: String
        }

        val clazzes: List<MyClazz>
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    @JvmDefaultWithCompatibility
    private interface BuilderUsingSameAliasForSameClazzInNestedBuilders {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myClazz")
        fun createClazz(@SetAsValue(alias = "myClazz", clazzProperty = "text") textValue: String)

        fun createClazzFromDefaultMethod() {
            createClazz("myTextFromDefaultMethod")
        }
    }

    @Test
    fun `test calling a default method that itself calls the builder then`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderUsingSameAliasForSameClazzInNestedBuilders::class) {
                    builder ->
                    builder.createClazzFromDefaultMethod()
                }
            }
        assertEquals(1, schemaInstance.clazzes.size)

        val myClazz = schemaInstance.clazzes.first()
        assertEquals("myTextFromDefaultMethod", myClazz.text)
    }
}
