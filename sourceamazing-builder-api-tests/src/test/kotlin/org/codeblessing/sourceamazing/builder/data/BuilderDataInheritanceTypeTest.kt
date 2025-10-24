package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataInheritanceTypeTest {

    private interface MyClazzes {

        interface MyClazz {
            val texts: List<String>
        }

        val clazzes: List<MyClazz>
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
    interface NestedBuilder {
        @BuilderMethod
        fun setText(
            @SetAsValue(alias = "myClazz", clazzProperty = "texts", modification = ClazzPropertyModification.ADD)
            textValue: String
        )
    }

    private interface BuilderWithTypeParameter<P, R> {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myClazz")
        fun createClazz(
            @SetAsValue(alias = "myClazz", clazzProperty = "texts", modification = ClazzPropertyModification.ADD)
            value: P
        ): R
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderExtendingInterfaceWithTypeParameter : BuilderWithTypeParameter<String, NestedBuilder>

    @Test
    fun `test using a sub-builder declared as type parameter`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderExtendingInterfaceWithTypeParameter::class) { builder ->
                    builder.createClazz("myFirstText").setText("mySecondText")
                }
            }
        assertEquals(1, schemaInstance.clazzes.size)

        val myClazz = schemaInstance.clazzes.first()
        assertEquals("myFirstText", myClazz.texts.first())
        assertEquals("mySecondText", myClazz.texts.last())
    }
}
