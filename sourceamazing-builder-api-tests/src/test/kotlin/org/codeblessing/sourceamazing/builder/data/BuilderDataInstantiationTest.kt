package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedClazzModelFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewClazzModel
import org.codeblessing.sourceamazing.builder.api.annotations.SetAsValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetClazzModelOfAlias
import org.codeblessing.sourceamazing.builder.api.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderDataInstantiationTest {

    private interface MyMixedClazzes {
        val myMixedClazzes: List<EnumMix>
    }

    enum class EnumMix(val enumId: String) {
        A("EnumMixA") {
            override val id: String
                get() = enumId
        },
        B("EnumMixB"),
        C("EnumMixC");

        open val id: String = enumId
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyMixedClazzes::class, alias = "root")
    private interface BuilderWithInstanceUsedAsModel {

        @BuilderMethod
        @NewClazzModel(clazz = EnumMix::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "myMixedClazzes", referencedAlias = "myClazz")
        fun createNewEnumClazzModel(
            @SetAsValue(clazzProperty = "id", alias = "myClazz") myId: String
        ): BuilderWithInstanceUsedAsModel
    }

    @Test
    fun `test insert instance-only clazzes as model should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.NOT_INSTANTIATABLE_CLAZZ) {
            SchemaApi.withSchema(MyMixedClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithInstanceUsedAsModel::class) { builder ->
                    builder.createNewEnumClazzModel("DataMixByModel")
                }
            }
        }
    }
}
