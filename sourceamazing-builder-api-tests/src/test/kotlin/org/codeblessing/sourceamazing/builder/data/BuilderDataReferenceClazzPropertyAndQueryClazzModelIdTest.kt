package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.UniqueId
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataReferenceClazzPropertyAndQueryClazzModelIdTest {

    private interface MyClasses {

        interface MyClazz {
            val id: String

            val clazzReferences: List<MyClazz>
        }

        val clazzes: List<MyClazz>
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClasses::class, alias = "root")
    private interface BuilderToAddReferences {

        @BuilderMethod
        @NewClazzModel(clazz = MyClasses.MyClazz::class, alias = "myClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "clazzes", referencedAlias = "myClazz")
        fun createClazz(
            @SetAsClazzModelId(alias = "myClazz") clazzModelId: UniqueId,
            @SetAsValue(alias = "myClazz", clazzProperty = "id") id: String,
        ): NestedBuilder

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClasses.MyClazz::class, alias = "myClazz")
        interface NestedBuilder {
            @BuilderMethod
            fun addReference(
                @SetClazzModelOfId(
                    clazzProperty = "clazzReferences",
                    alias = "myClazz",
                    modification = ClazzPropertyModification.ADD,
                )
                @IgnoreNullValue
                myReference: UniqueId?
            ): NestedBuilder
        }
    }

    @Test
    fun `test add reference to itself`() {
        val selfReferencingClazzModelId = UniqueId.of("Self-Referencing-Id")
        val schemaInstance: MyClasses =
            SchemaApi.withSchema(MyClasses::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                    builder
                        .createClazz(selfReferencingClazzModelId, selfReferencingClazzModelId.name)
                        .addReference(selfReferencingClazzModelId)
                }
            }

        assertEquals(1, schemaInstance.clazzes.size)
        val clazz = schemaInstance.clazzes.first()
        assertEquals(selfReferencingClazzModelId.name, clazz.id)
        assertEquals(1, clazz.clazzReferences.size)
        assertEquals(selfReferencingClazzModelId.name, clazz.clazzReferences[0].id)
    }

    @Test
    fun `test add main clazz referencing two other clazzes`() {
        val mainClazzModelId = UniqueId.of("Main-Id")
        val firstReferencedClazzModelId = UniqueId.of("First-Referenced-Id")
        val secondReferencedClazzModelId = UniqueId.of("Second-Referenced-Id")

        val schemaInstance: MyClasses =
            SchemaApi.withSchema(MyClasses::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                    builder
                        .createClazz(mainClazzModelId, mainClazzModelId.name)
                        .addReference(firstReferencedClazzModelId)
                        .addReference(secondReferencedClazzModelId)
                    builder.createClazz(firstReferencedClazzModelId, firstReferencedClazzModelId.name)
                    builder.createClazz(secondReferencedClazzModelId, secondReferencedClazzModelId.name)
                }
            }

        assertEquals(3, schemaInstance.clazzes.size)
        val mainClazz = schemaInstance.clazzes.first { it.id == mainClazzModelId.name }
        assertEquals(mainClazzModelId.name, mainClazz.id)
        assertEquals(2, mainClazz.clazzReferences.size)

        val firstReferencedClazz = mainClazz.clazzReferences[0]
        val secondReferencedClazz = mainClazz.clazzReferences[1]

        assertEquals(firstReferencedClazzModelId.name, firstReferencedClazz.id)
        assertEquals(secondReferencedClazzModelId.name, secondReferencedClazz.id)

        assertEquals(0, firstReferencedClazz.clazzReferences.size)
        assertEquals(0, secondReferencedClazz.clazzReferences.size)
    }

    @Test
    fun `test three clazz referencing each other in a circle`() {
        val firstClazzModelId = UniqueId.of("First-Referenced-Id")
        val secondClazzModelId = UniqueId.of("Second-Referenced-Id")
        val thirdClazzModelId = UniqueId.of("Third-Referenced-Id")

        val schemaInstance: MyClasses =
            SchemaApi.withSchema(MyClasses::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                    builder.createClazz(firstClazzModelId, firstClazzModelId.name).addReference(secondClazzModelId)
                    builder.createClazz(secondClazzModelId, secondClazzModelId.name).addReference(thirdClazzModelId)
                    builder.createClazz(thirdClazzModelId, thirdClazzModelId.name).addReference(firstClazzModelId)
                }
            }

        assertEquals(3, schemaInstance.clazzes.size)

        val firstClazz = schemaInstance.clazzes.first { it.id == firstClazzModelId.name }
        val secondClazz = schemaInstance.clazzes.first { it.id == secondClazzModelId.name }
        val thirdClazz = schemaInstance.clazzes.first { it.id == thirdClazzModelId.name }

        assertEquals(firstClazzModelId.name, firstClazz.id)
        assertEquals(secondClazzModelId.name, secondClazz.id)
        assertEquals(thirdClazzModelId.name, thirdClazz.id)

        assertEquals(secondClazzModelId.name, firstClazz.clazzReferences.first().id)
        assertEquals(thirdClazzModelId.name, secondClazz.clazzReferences.first().id)
        assertEquals(firstClazzModelId.name, thirdClazz.clazzReferences.first().id)
    }

    @Test
    fun `test capability to return chains of referenced clazz instances`() {
        val firstClazzModelId = UniqueId.of("First-Referenced-Id")
        val secondClazzModelId = UniqueId.of("Second-Referenced-Id")
        val thirdClazzModelId = UniqueId.of("Third-Referenced-Id")

        val schemaInstance: MyClasses =
            SchemaApi.withSchema(MyClasses::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                    builder.createClazz(firstClazzModelId, firstClazzModelId.name).addReference(secondClazzModelId)
                    builder.createClazz(secondClazzModelId, secondClazzModelId.name).addReference(thirdClazzModelId)
                    builder.createClazz(thirdClazzModelId, thirdClazzModelId.name).addReference(firstClazzModelId)
                }
            }

        assertEquals(3, schemaInstance.clazzes.size)

        val firstClazz = schemaInstance.clazzes.first { it.id == firstClazzModelId.name }

        assertEquals(
            secondClazzModelId.name,
            firstClazz.clazzReferences
                .first()
                .clazzReferences
                .first()
                .clazzReferences
                .first()
                .clazzReferences
                .first()
                .id,
        )
    }

    @Test
    fun `test referencing an unknown clazz instance throws an exception`() {
        val mainClazzModelId = UniqueId.of("Main-Id")
        val instantiatedClazzModelId = UniqueId.of("Instantiated-Referenced-Id")
        val uninstantiatedClazzModelId = UniqueId.of("Uninstantiated-Referenced-Id")

        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.MISSING_REFERENCED_CLAZZ_CLAZZ_PROPERTY_VALUE,
        ) {
            SchemaApi.withSchema(MyClasses::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                    builder
                        .createClazz(mainClazzModelId, mainClazzModelId.name)
                        .addReference(instantiatedClazzModelId)
                        .addReference(uninstantiatedClazzModelId)
                    builder.createClazz(instantiatedClazzModelId, instantiatedClazzModelId.name)
                }
            }
        }
    }
}
