package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderDataAddOrReplaceNonNullAndNullReferencesTest {

    private interface MyClazzes {

        interface MyClazz {
            val id: MyClazzId
            val myReferencedClazzes: List<MyClazz>
        }

        val clazz: MyClazz
    }

    enum class MyClazzId {
        FIRST_ID,
        SECOND_ID,
        THIRD_ID,
        FOURTH_ID,
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderToAddOrReplaceClazzPropertiesByReference {

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazz", referencedAlias = "myClazz")
        fun createAndAttachMyClazz(
            @SetAsClazzModelId(alias = "myClazz") @SetAsValue(clazzProperty = "id", alias = "myClazz") id: MyClazzId
        ): NestedBuilder

        @BuilderMethod
        @NewClazzModel(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        fun createMyClazz(
            @SetAsClazzModelId(alias = "myClazz") @SetAsValue(clazzProperty = "id", alias = "myClazz") id: MyClazzId
        ): BuilderToAddOrReplaceClazzPropertiesByReference

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyClazz::class, alias = "myClazz")
        interface NestedBuilder {
            @BuilderMethod
            fun setReference(
                @SetClazzModelOfId(
                    clazzProperty = "myReferencedClazzes",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.REPLACE,
                )
                myReference: MyClazzId
            ): NestedBuilder

            @BuilderMethod
            fun setNullableReference(
                @SetClazzModelOfId(
                    clazzProperty = "myReferencedClazzes",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.REPLACE,
                )
                @IgnoreNullValue
                myNullableReference: MyClazzId?
            ): NestedBuilder

            @BuilderMethod
            fun setReferences(
                @SetClazzModelOfId(
                    clazzProperty = "myReferencedClazzes",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.REPLACE,
                )
                myReferences: List<MyClazzId>
            ): NestedBuilder

            @BuilderMethod
            fun setVarargReferences(
                @SetClazzModelOfId(
                    clazzProperty = "myReferencedClazzes",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.REPLACE,
                )
                vararg myReferences: MyClazzId
            ): NestedBuilder

            @BuilderMethod
            fun setNullableReferences(
                @SetClazzModelOfId(
                    clazzProperty = "myReferencedClazzes",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.REPLACE,
                )
                @IgnoreNullValue
                myReferences: List<MyClazzId?>
            ): NestedBuilder

            @BuilderMethod
            fun setNullableVarargReferences(
                @SetClazzModelOfId(
                    clazzProperty = "myReferencedClazzes",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.REPLACE,
                )
                @IgnoreNullValue
                vararg myReferences: MyClazzId?
            ): NestedBuilder

            @BuilderMethod
            fun addReference(
                @SetClazzModelOfId(
                    clazzProperty = "myReferencedClazzes",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.ADD,
                )
                myReference: MyClazzId
            ): NestedBuilder

            @BuilderMethod
            fun addNullableReference(
                @SetClazzModelOfId(
                    clazzProperty = "myReferencedClazzes",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.ADD,
                )
                @IgnoreNullValue
                myNullableReference: MyClazzId?
            ): NestedBuilder

            @BuilderMethod
            fun addReferences(
                @SetClazzModelOfId(
                    clazzProperty = "myReferencedClazzes",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.ADD,
                )
                myReferences: List<MyClazzId>
            ): NestedBuilder

            @BuilderMethod
            fun addVarargReferences(
                @SetClazzModelOfId(
                    clazzProperty = "myReferencedClazzes",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.ADD,
                )
                vararg myReferences: MyClazzId
            ): NestedBuilder

            @BuilderMethod
            fun addNullableReferences(
                @SetClazzModelOfId(
                    clazzProperty = "myReferencedClazzes",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.ADD,
                )
                @IgnoreNullValue
                myReferences: List<MyClazzId?>
            ): NestedBuilder

            @BuilderMethod
            fun addNullableVarargReferences(
                @SetClazzModelOfId(
                    clazzProperty = "myReferencedClazzes",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.ADD,
                )
                @IgnoreNullValue
                vararg myReferences: MyClazzId?
            ): NestedBuilder
        }
    }

    @Test
    fun `test insert a single reference to the same myReferencedClazzes clazzProperty multiple times with REPLACE mode does always clear and override the result`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext = schemaContext,
                    builderClass = BuilderToAddOrReplaceClazzPropertiesByReference::class,
                ) { builder ->
                    builder
                        .createMyClazz(MyClazzId.SECOND_ID)
                        .createMyClazz(MyClazzId.THIRD_ID)
                        .createMyClazz(MyClazzId.FOURTH_ID)
                        .createAndAttachMyClazz(MyClazzId.FIRST_ID)
                        .setReference(MyClazzId.SECOND_ID)
                        .setReference(MyClazzId.THIRD_ID)
                        .setReference(MyClazzId.FOURTH_ID)
                }
            }

        val clazz = schemaInstance.clazz
        Assertions.assertEquals(listOf(MyClazzId.FOURTH_ID), clazz.myReferencedClazzes.map { it.id })
    }

    @Test
    fun `test insert a list of references to myReferencedClazzes clazzProperty with REPLACE mode does replace with all list entries`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext = schemaContext,
                    builderClass = BuilderToAddOrReplaceClazzPropertiesByReference::class,
                ) { builder ->
                    builder
                        .createMyClazz(MyClazzId.SECOND_ID)
                        .createMyClazz(MyClazzId.THIRD_ID)
                        .createMyClazz(MyClazzId.FOURTH_ID)
                        .createAndAttachMyClazz(MyClazzId.FIRST_ID)
                        .setReferences(listOf(MyClazzId.SECOND_ID, MyClazzId.THIRD_ID))
                        .setVarargReferences(MyClazzId.THIRD_ID, MyClazzId.FOURTH_ID)
                }
            }

        val clazz = schemaInstance.clazz
        Assertions.assertEquals(
            listOf(MyClazzId.THIRD_ID, MyClazzId.FOURTH_ID),
            clazz.myReferencedClazzes.map { it.id },
        )
    }

    @Test
    fun `test insert a list of references and null values to myReferencedClazzes clazzProperty with REPLACE mode does replace with all list entries that are not null`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzPropertiesByReference::class) { builder
                    ->
                    builder
                        .createMyClazz(MyClazzId.SECOND_ID)
                        .createMyClazz(MyClazzId.THIRD_ID)
                        .createMyClazz(MyClazzId.FOURTH_ID)
                        .createAndAttachMyClazz(MyClazzId.FIRST_ID)
                        .setNullableReferences(listOf(MyClazzId.SECOND_ID, null, MyClazzId.THIRD_ID))
                        .setNullableVarargReferences(MyClazzId.THIRD_ID, null, MyClazzId.FOURTH_ID)
                }
            }

        val clazz = schemaInstance.clazz
        Assertions.assertEquals(
            listOf(MyClazzId.THIRD_ID, MyClazzId.FOURTH_ID),
            clazz.myReferencedClazzes.map { it.id },
        )
    }

    @Test
    fun `test insert an empty list of references to myReferencedClazzes clazzProperty with REPLACE mode does replace with an empty list`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzPropertiesByReference::class) { builder
                    ->
                    builder
                        .createMyClazz(MyClazzId.SECOND_ID)
                        .createMyClazz(MyClazzId.THIRD_ID)
                        .createMyClazz(MyClazzId.FOURTH_ID)
                        .createAndAttachMyClazz(MyClazzId.FIRST_ID)
                        .setReferences(listOf(MyClazzId.SECOND_ID, MyClazzId.THIRD_ID))
                        .setReferences(emptyList())
                        .setVarargReferences()
                }
            }

        val clazz = schemaInstance.clazz
        Assertions.assertEquals(emptyList<MyClazzId>(), clazz.myReferencedClazzes.map { it.id })
    }

    @Test
    fun `test insert null values to a myReferencedClazzes clazzProperty with REPLACE mode does not clear and override the result for null values`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzPropertiesByReference::class) { builder
                    ->
                    builder
                        .createMyClazz(MyClazzId.SECOND_ID)
                        .createMyClazz(MyClazzId.THIRD_ID)
                        .createMyClazz(MyClazzId.FOURTH_ID)
                        .createAndAttachMyClazz(MyClazzId.FIRST_ID)
                        .setNullableReference(MyClazzId.SECOND_ID)
                        .setNullableReference(null)
                        .setNullableReference(MyClazzId.THIRD_ID)
                        .setNullableReference(null)
                }
            }

        val clazz = schemaInstance.clazz
        Assertions.assertEquals(listOf(MyClazzId.THIRD_ID), clazz.myReferencedClazzes.map { it.id })
    }

    @Test
    fun `test insert a single reference to the same myReferencedClazzes clazzProperty multiple times with ADD mode does append`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzPropertiesByReference::class) { builder
                    ->
                    builder
                        .createMyClazz(MyClazzId.SECOND_ID)
                        .createMyClazz(MyClazzId.THIRD_ID)
                        .createMyClazz(MyClazzId.FOURTH_ID)
                        .createAndAttachMyClazz(MyClazzId.FIRST_ID)
                        .addReference(MyClazzId.SECOND_ID)
                        .addReference(MyClazzId.SECOND_ID)
                        .addReference(MyClazzId.THIRD_ID)
                        .addReference(MyClazzId.FOURTH_ID)
                }
            }

        val clazz = schemaInstance.clazz
        Assertions.assertEquals(
            listOf(MyClazzId.SECOND_ID, MyClazzId.SECOND_ID, MyClazzId.THIRD_ID, MyClazzId.FOURTH_ID),
            clazz.myReferencedClazzes.map { it.id },
        )
    }

    @Test
    fun `test insert a list of references to myReferencedClazzes clazzProperty with ADD mode does append`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzPropertiesByReference::class) { builder
                    ->
                    builder
                        .createMyClazz(MyClazzId.SECOND_ID)
                        .createMyClazz(MyClazzId.THIRD_ID)
                        .createMyClazz(MyClazzId.FOURTH_ID)
                        .createAndAttachMyClazz(MyClazzId.FIRST_ID)
                        .addReferences(listOf(MyClazzId.SECOND_ID, MyClazzId.THIRD_ID))
                        .addVarargReferences(MyClazzId.THIRD_ID, MyClazzId.FOURTH_ID)
                }
            }

        val clazz = schemaInstance.clazz
        Assertions.assertEquals(
            listOf(MyClazzId.SECOND_ID, MyClazzId.THIRD_ID, MyClazzId.THIRD_ID, MyClazzId.FOURTH_ID),
            clazz.myReferencedClazzes.map { it.id },
        )
    }

    @Test
    fun `test insert a list of references and null values to myReferencedClazzes clazzProperty with ADD mode does append all non-null values`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzPropertiesByReference::class) { builder
                    ->
                    builder
                        .createMyClazz(MyClazzId.SECOND_ID)
                        .createMyClazz(MyClazzId.THIRD_ID)
                        .createMyClazz(MyClazzId.FOURTH_ID)
                        .createAndAttachMyClazz(MyClazzId.FIRST_ID)
                        .addNullableReferences(listOf(MyClazzId.SECOND_ID, null, MyClazzId.THIRD_ID, null))
                        .addNullableVarargReferences(MyClazzId.THIRD_ID, null, MyClazzId.FOURTH_ID, null)
                }
            }

        val clazz = schemaInstance.clazz
        Assertions.assertEquals(
            listOf(MyClazzId.SECOND_ID, MyClazzId.THIRD_ID, MyClazzId.THIRD_ID, MyClazzId.FOURTH_ID),
            clazz.myReferencedClazzes.map { it.id },
        )
    }

    @Test
    fun `test insert an empty list of references to myReferencedClazzes clazzProperty with ADD mode does not change the clazzProperty values`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzPropertiesByReference::class) { builder
                    ->
                    builder
                        .createMyClazz(MyClazzId.SECOND_ID)
                        .createMyClazz(MyClazzId.THIRD_ID)
                        .createMyClazz(MyClazzId.FOURTH_ID)
                        .createAndAttachMyClazz(MyClazzId.FIRST_ID)
                        .addReference(MyClazzId.SECOND_ID)
                        .addReferences(emptyList())
                        .addNullableReferences(emptyList())
                        .addNullableVarargReferences()
                }
            }

        val clazz = schemaInstance.clazz
        Assertions.assertEquals(listOf(MyClazzId.SECOND_ID), clazz.myReferencedClazzes.map { it.id })
    }

    @Test
    fun `test insert null values to the same myReferencedClazzes clazzProperty multiple times with ADD mode does not append the null values`() {
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceClazzPropertiesByReference::class) { builder
                    ->
                    builder
                        .createMyClazz(MyClazzId.SECOND_ID)
                        .createMyClazz(MyClazzId.THIRD_ID)
                        .createMyClazz(MyClazzId.FOURTH_ID)
                        .createAndAttachMyClazz(MyClazzId.FIRST_ID)
                        .addReference(MyClazzId.SECOND_ID)
                        .addNullableReference(MyClazzId.THIRD_ID)
                        .addNullableReference(null)
                        .addNullableReference(MyClazzId.FOURTH_ID)
                        .addNullableReference(null)
                }
            }

        val clazz = schemaInstance.clazz
        Assertions.assertEquals(
            listOf(MyClazzId.SECOND_ID, MyClazzId.THIRD_ID, MyClazzId.FOURTH_ID),
            clazz.myReferencedClazzes.map { it.id },
        )
    }
}
