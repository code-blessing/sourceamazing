package org.codeblessing.sourceamazing.builder.factories

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.BuilderContext
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.api.by
import org.codeblessing.sourceamazing.builder.api.create
import org.codeblessing.sourceamazing.builder.factories.BuilderFactoriesDataTest.MyClazzBuilderAnnotated.NestedNumberBuilderAnnotated
import org.codeblessing.sourceamazing.builder.factories.BuilderFactoriesDataTest.MyClazzBuilderAnnotated.NestedTextBuilderAnnotated
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderFactoriesDataTest {

    private interface MyClazzes {

        interface MyClazz {
            val texts: List<String>

            val numbers: List<Int>
        }

        val clazzes: List<MyClazz>
    }

    interface MyClazzBuilder {
        fun createClazz(): NestedTextBuilder

        fun createAnotherClazzWithNestedBuilder(): NestedTextBuilder

        fun createAnotherClazz()
    }

    interface NestedTextBuilder {
        fun setText(textValue: String): NestedNumberBuilder
    }

    interface NestedNumberBuilder {
        fun setNumber(numberValue: Int): NestedTextBuilder

        fun setNumberPlusSeven(numberValue: Int): NestedTextBuilder
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    interface MyClazzBuilderAnnotated : MyClazzBuilder {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myClazz")
        override fun createClazz(): NestedTextBuilderAnnotated

        override fun createAnotherClazzWithNestedBuilder(): NestedTextBuilderAnnotated

        override fun createAnotherClazz()

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        interface NestedTextBuilderAnnotated : NestedTextBuilder {
            @BuilderMethod
            override fun setText(
                @SetAsValue(alias = "myClazz", clazzProperty = "texts") textValue: String
            ): NestedNumberBuilderAnnotated
        }

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        interface NestedNumberBuilderAnnotated : NestedNumberBuilder {
            @BuilderMethod
            override fun setNumber(
                @SetAsValue(alias = "myClazz", clazzProperty = "numbers") numberValue: Int
            ): NestedTextBuilderAnnotated

            override fun setNumberPlusSeven(numberValue: Int): NestedTextBuilderAnnotated
        }
    }

    private class MyClazzBuilderAnnotatedImpl(
        private val proxyBuilder: MyClazzBuilderAnnotated,
        private val schemaContext: SchemaContext,
        private val builderContext: BuilderContext,
    ) : MyClazzBuilderAnnotated by proxyBuilder {

        override fun createAnotherClazz() {
            val clazzData = schemaContext.dataCollector.newClazzModel(MyClazzes.MyClazz::class)
            clazzData.addClazzPropertyValue("texts", "hallo")
            clazzData.addClazzPropertyValue("numbers", 42)
            val rootClazzData = schemaContext.dataCollector.existingClazzModel(builderContext.getClazzModelId("root"))

            rootClazzData.addClazzPropertyReference("clazzes", clazzData)
        }

        override fun createAnotherClazzWithNestedBuilder(): NestedTextBuilderAnnotated {
            return proxyBuilder.createClazz()
        }
    }

    private class NestedNumberBuilderImpl(
        private val proxyBuilder: NestedNumberBuilderAnnotated,
        private val schemaContext: SchemaContext,
        private val builderContext: BuilderContext,
    ) : NestedNumberBuilderAnnotated by proxyBuilder {

        override fun setNumberPlusSeven(numberValue: Int): NestedTextBuilderAnnotated {
            return proxyBuilder.setNumber(numberValue + 7)
        }
    }

    private fun useBuilder(builder: MyClazzBuilder) {
        builder.createClazz().setText("Added1").setNumber(17).setText("Added2").setNumber(23).setText("Added3")

        builder.createAnotherClazz()

        builder
            .createAnotherClazzWithNestedBuilder()
            .setText("Second-Added1")
            .setNumber(77)
            .setText("Second-Added2")
            .setNumberPlusSeven(81)
    }

    @Test
    fun `test mixing builders and builder implementations and adding data to it`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    MyClazzBuilderAnnotated::class,
                    setOf(
                        MyClazzBuilderAnnotated::class by MyClazzBuilderAnnotatedImpl::class,
                        NestedNumberBuilderAnnotated::class.create(NestedNumberBuilderImpl::class) {
                            builder,
                            _,
                            builderContext ->
                            NestedNumberBuilderImpl(builder, schemaContext, builderContext)
                        },
                    ),
                ) { builder ->
                    useBuilder(builder)
                }
            }
        Assertions.assertEquals(3, schemaInstance.clazzes.size)

        val myClazz1 = schemaInstance.clazzes[0]
        Assertions.assertEquals(listOf(17, 23), myClazz1.numbers)
        Assertions.assertEquals(listOf("Added1", "Added2", "Added3"), myClazz1.texts)

        val myClazz2 = schemaInstance.clazzes[1]
        Assertions.assertEquals(listOf(42), myClazz2.numbers)
        Assertions.assertEquals(listOf("hallo"), myClazz2.texts)

        val myClazz3 = schemaInstance.clazzes[2]
        Assertions.assertEquals(listOf(77, 88), myClazz3.numbers)
        Assertions.assertEquals(listOf("Second-Added1", "Second-Added2"), myClazz3.texts)
    }
}
