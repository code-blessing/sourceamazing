package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.api.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Suppress("UNUSED")
class BuilderDataEnumTest {
    enum class AllDatatypesEnum {
        STRING,
        INT,
        FLOAT,
        DOUBLE,
        UUID,
    }

    private interface MyClazzes {

        interface MyClazz {

            val enumClazzPropertyValue: AllDatatypesEnum
        }

        val clazz: MyClazz
    }

    enum class ExactCopyOfAllDatatypesEnum {
        STRING,
        INT,
        FLOAT,
        DOUBLE,
        UUID,
    }

    enum class ValueCompatibleNumericDatatypesEnum {
        INT,
        FLOAT,
        DOUBLE,
    }

    enum class IncompatibleWithNumericDatatypesEnum {
        INT,
        FLOAT,
        DOUBLE,
        BYTE, // this is an incompatible clazzProperty value
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderMethodWithAllDatatypesEnum {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazz", referencedAlias = "myClazz")
        fun doSetEnumValue(
            @SetAsValue(alias = "myClazz", clazzProperty = "enumClazzPropertyValue") enumValue: AllDatatypesEnum
        )
    }

    @Test
    fun `test using the enum type defined on the clazzProperty to set the enum value should not fail`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithAllDatatypesEnum::class) { builder ->
                    builder.doSetEnumValue(AllDatatypesEnum.INT)
                }
            }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.clazz.enumClazzPropertyValue)
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderMethodWithCompatibleNumericDatatypesEnum {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazz", referencedAlias = "myClazz")
        fun doSetEnumValue(
            @SetAsValue(alias = "myClazz", clazzProperty = "enumClazzPropertyValue")
            enumValue: ValueCompatibleNumericDatatypesEnum
        )
    }

    @Test
    fun `test using a enum type not defined on the clazzProperty but with subset of all enum values to set the enum value should throw an exception`() {
        assertThrows<BuilderSyntaxException> {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithCompatibleNumericDatatypesEnum::class) { builder
                    ->
                    builder.doSetEnumValue(ValueCompatibleNumericDatatypesEnum.INT)
                }
            }
        }
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderMethodWithExactCopyOfAllDatatypesEnum {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazz", referencedAlias = "myClazz")
        fun doSetEnumValue(
            @SetAsValue(alias = "myClazz", clazzProperty = "enumClazzPropertyValue")
            enumValue: ExactCopyOfAllDatatypesEnum
        )
    }

    @Test
    fun `test using a enum type not defined on the clazzProperty but with exactly equal enum values to set the enum value should throw an exception`() {
        assertThrows<BuilderSyntaxException> {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithExactCopyOfAllDatatypesEnum::class) { builder ->
                    builder.doSetEnumValue(ExactCopyOfAllDatatypesEnum.INT)
                }
            }
        }
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderMethodWithIncompatibleWithAllDatatypesEnum {

        @BuilderMethod
        @NewClazzModel(MyClazzes.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazz", referencedAlias = "myClazz")
        fun doSetEnumValue(
            @SetAsValue(alias = "myClazz", clazzProperty = "enumClazzPropertyValue")
            enumValue: IncompatibleWithNumericDatatypesEnum
        )
    }

    @Test
    fun `test using a enum type not defined on the clazzProperty but with a incompatible subset of enum values to set the enum value should throw an exception`() {
        assertThrows<BuilderSyntaxException> {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithIncompatibleWithAllDatatypesEnum::class) {
                    builder ->
                    builder.doSetEnumValue(IncompatibleWithNumericDatatypesEnum.INT)
                }
            }
        }
    }
}
