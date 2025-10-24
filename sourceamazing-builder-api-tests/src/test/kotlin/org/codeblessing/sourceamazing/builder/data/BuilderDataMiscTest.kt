package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedClazzModelFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewClazzModel
import org.codeblessing.sourceamazing.builder.api.annotations.SetAsClazzModelId
import org.codeblessing.sourceamazing.builder.api.annotations.SetAsValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetClazzModelOfAlias
import org.codeblessing.sourceamazing.builder.api.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.UniqueId
import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderDataMiscTest {

    private interface MyMixedClazzes {
        val myMixedClazzes: List<MixedClazz>
    }

    sealed interface MixedClazz {
        val id: String
    }

    enum class EnumMix(val enumId: String) : MixedClazz {
        A("EnumMixA") {
            override val id: String
                get() = enumId
        },
        B("EnumMixB"),
        C("EnumMixC");

        override val id: String = enumId
    }

    data class DataMix(override val id: String) : MixedClazz

    interface SubInterfaceMix : MixedClazz

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyMixedClazzes::class, alias = "root")
    private interface BuilderForMixedModelAndInstance {

        @BuilderMethod
        @NewClazzModel(clazz = SubInterfaceMix::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "myMixedClazzes", referencedAlias = "myClazz")
        fun createNewClazzModel(
            @SetAsValue(clazzProperty = "id", alias = "myClazz") myId: String
        ): BuilderForMixedModelAndInstance

        @BuilderMethod
        @NewClazzModel(clazz = DataMix::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "myMixedClazzes", referencedAlias = "myClazz")
        fun createNewDataClazzModel(
            @SetAsValue(clazzProperty = "id", alias = "myClazz") myId: String
        ): BuilderForMixedModelAndInstance

        @BuilderMethod
        fun addInstance(
            @SetAsValue(alias = "root", clazzProperty = "myMixedClazzes") myMixedClazz: MixedClazz
        ): BuilderForMixedModelAndInstance
    }

    @Test
    fun `test insert values for all the different types of clazzProperties does not fail`() {
        val schemaInstance: MyMixedClazzes =
            SchemaApi.withSchema(MyMixedClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderForMixedModelAndInstance::class) { builder ->
                    builder
                        .addInstance(EnumMix.A)
                        .addInstance(EnumMix.B)
                        .addInstance(DataMix("DataMix"))
                        .createNewClazzModel("SubInterfaceMix")
                }
            }

        Assertions.assertEquals(
            listOf("EnumMixA", "EnumMixB", "DataMix", "SubInterfaceMix"),
            schemaInstance.myMixedClazzes.map { it.id },
        )
    }

    private interface MyClazzes {
        val myClazzes: List<MyClazz>
    }

    interface MyClazz {
        val id: String
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderWithWrongUseOfClazzPropertyValueAndClazzPropertyReference {

        @BuilderMethod
        @NewClazzModel(clazz = SubInterfaceMix::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "myClazzes", referencedAlias = "myClazz")
        fun createNewClazzModel(
            @SetAsClazzModelId(alias = "myClazz") myId: UniqueId
        ): NestedBuilderWithWrongUseOfClazzPropertyValueAndClazzPropertyReference

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazz::class, alias = "myClazz")
        interface NestedBuilderWithWrongUseOfClazzPropertyValueAndClazzPropertyReference {

            @BuilderMethod fun addIdClazzProperty(@SetAsValue(clazzProperty = "id", alias = "myClazz") myId: UniqueId)
        }
    }

    @Test
    fun `test insert a clazzProperty reference with @SetClazzPropertyValue instead of @SetClazzPropertyReference throws an exception`() {
        val clazzModelId = UniqueId.of("Clazz")
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.UNKNOWN_CLAZZ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderWithWrongUseOfClazzPropertyValueAndClazzPropertyReference::class,
                ) { builder ->
                    builder.createNewClazzModel(clazzModelId).addIdClazzProperty(clazzModelId)
                }
            }
        }
    }

    private interface MyClazzesWithTypedId {
        val myClazzes: List<MyClazzWithTypedId>
    }

    interface MyClazzWithTypedId {
        val id: UniqueId
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzesWithTypedId::class, alias = "root")
    private interface BuilderWithTypedIds {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzWithTypedId::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "myClazzes", referencedAlias = "myClazz")
        fun createNewClazzModel(
            @SetAsClazzModelId(alias = "myClazz") @SetAsValue(alias = "myClazz", clazzProperty = "id") myId: UniqueId
        )
    }

    @Test
    fun `test insert a clazz model identifier with @SetClazzModelIdValue and a clazzProperty value with @SetClazzPropertyValue in one builder method call should not fail`() {
        val clazzModelId = UniqueId.of("Clazz")
        val schemaInstance =
            SchemaApi.withSchema(MyClazzesWithTypedId::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithTypedIds::class) { builder ->
                    builder.createNewClazzModel(clazzModelId)
                }
            }

        Assertions.assertEquals(clazzModelId, schemaInstance.myClazzes.first().id)
    }
}
