package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.NestedClassesBuilderTest.NestedClazzesSchema.BuiltinFieldTypeClazz.BuiltinTypeEnum
import org.codeblessing.sourceamazing.builder.NestedClassesBuilderTest.NestedClazzesSchema.CollectionOfValuesFieldClazz.CollectionKindEnum
import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class NestedClassesBuilderTest {

    data class BusinessObjectId(val name: String) {
        companion object {
            fun of(id: String): BusinessObjectId {
                return BusinessObjectId(id)
            }
        }
    }

    private interface NestedClazzesSchema {

        val businessObjects: List<BusinessObjectClazz>

        interface BusinessObjectClazz {
            val name: String

            val fields: List<Field>
        }

        sealed interface Field {
            val fieldName: String
        }

        interface SingleValueFieldClazz : Field {
            val nullable: Boolean

            val singleValueType: FieldType
        }

        interface CollectionOfValuesFieldClazz : Field {
            val collectionKind: CollectionKindEnum

            enum class CollectionKindEnum {
                LIST,
                SET,
            }

            val collectionValuesType: FieldType
        }

        sealed interface FieldType

        interface BuiltinFieldTypeClazz : FieldType {
            val builtinType: BuiltinTypeEnum

            enum class BuiltinTypeEnum {
                STRING,
                INTEGER,
            }
        }

        interface ReferenceFieldTypeClazz : FieldType {
            val referencedBusinessObject: BusinessObjectClazz
        }
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = NestedClazzesSchema::class, alias = "root")
    private interface NestedObjectsBuilder {

        @BuilderMethod
        @NewClazzModel(NestedClazzesSchema.BusinessObjectClazz::class, alias = "bizObj")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "businessObjects", referencedAlias = "bizObj")
        fun newBusinessObject(
            @SetAsClazzModelId(alias = "bizObj") clazzModelId: BusinessObjectId,
            @SetAsValue(alias = "bizObj", clazzProperty = "name") name: String,
            @InjectBuilder builder: BusinessObjectClazzBuilder.() -> Unit,
        )

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = NestedClazzesSchema.BusinessObjectClazz::class, alias = "bizObj")
        interface BusinessObjectClazzBuilder {

            @BuilderMethod
            @NewClazzModel(NestedClazzesSchema.SingleValueFieldClazz::class, alias = "field")
            @SetClazzModelOfAlias(alias = "bizObj", clazzProperty = "fields", referencedAlias = "field")
            fun addSingleValueField(
                @SetAsValue(alias = "field", clazzProperty = "fieldName") fieldName: String,
                @SetAsValue(alias = "field", clazzProperty = "nullable") nullable: Boolean = false,
                @InjectBuilder builder: FieldTypeForSingleFieldClazzBuilder.() -> Unit,
            )

            @BuilderMethod
            @NewClazzModel(NestedClazzesSchema.CollectionOfValuesFieldClazz::class, alias = "field")
            @SetClazzModelOfAlias(alias = "bizObj", clazzProperty = "fields", referencedAlias = "field")
            fun addCollectionOfValuesField(
                @SetAsValue(alias = "field", clazzProperty = "fieldName") fieldName: String,
                @SetAsValue(alias = "field", clazzProperty = "collectionKind") collectionKind: CollectionKindEnum,
                @InjectBuilder builder: FieldTypeForCollectionFieldClazzBuilder.() -> Unit,
            )
        }

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(
            clazz = NestedClazzesSchema.SingleValueFieldClazz::class,
            alias = "field",
        )
        interface FieldTypeForSingleFieldClazzBuilder {

            @BuilderMethod
            @NewClazzModel(NestedClazzesSchema.BuiltinFieldTypeClazz::class, alias = "fieldType")
            @SetClazzModelOfAlias(alias = "field", clazzProperty = "singleValueType", referencedAlias = "fieldType")
            fun builtinType(
                @SetAsValue(alias = "fieldType", clazzProperty = "builtinType") builtinType: BuiltinTypeEnum
            )

            @BuilderMethod
            @NewClazzModel(NestedClazzesSchema.ReferenceFieldTypeClazz::class, alias = "fieldType")
            @SetClazzModelOfAlias(alias = "field", clazzProperty = "singleValueType", referencedAlias = "fieldType")
            fun reference(
                @SetClazzModelOfId(alias = "fieldType", clazzProperty = "referencedBusinessObject") id: BusinessObjectId
            )
        }

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(
            clazz = NestedClazzesSchema.CollectionOfValuesFieldClazz::class,
            alias = "field",
        )
        interface FieldTypeForCollectionFieldClazzBuilder {

            @BuilderMethod
            @NewClazzModel(NestedClazzesSchema.BuiltinFieldTypeClazz::class, alias = "fieldType")
            @SetClazzModelOfAlias(
                alias = "field",
                clazzProperty = "collectionValuesType",
                referencedAlias = "fieldType",
            )
            fun builtinType(
                @SetAsValue(alias = "fieldType", clazzProperty = "builtinType") builtinType: BuiltinTypeEnum
            )

            @BuilderMethod
            @NewClazzModel(NestedClazzesSchema.ReferenceFieldTypeClazz::class, alias = "fieldType")
            @SetClazzModelOfAlias(
                alias = "field",
                clazzProperty = "collectionValuesType",
                referencedAlias = "fieldType",
            )
            fun reference(
                @SetClazzModelOfId(alias = "fieldType", clazzProperty = "referencedBusinessObject") id: BusinessObjectId
            )
        }
    }

    @Test
    fun `test nesting of clazzes`() {
        val personBo = BusinessObjectId.of("Person")

        val schemaInstance: NestedClazzesSchema =
            SchemaApi.withSchema(NestedClazzesSchema::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, NestedObjectsBuilder::class) { builder ->
                    builder.newBusinessObject(personBo, "the person business object") {
                        addSingleValueField("firstname") { builtinType(BuiltinTypeEnum.STRING) }

                        addSingleValueField("age") { builtinType(BuiltinTypeEnum.INTEGER) }
                        addSingleValueField("spouse") { reference(personBo) }
                        addCollectionOfValuesField("nicknames", CollectionKindEnum.LIST) {
                            builtinType(BuiltinTypeEnum.STRING)
                        }
                        addCollectionOfValuesField("children", CollectionKindEnum.SET) { reference(personBo) }
                    }
                }
            }

        assertEquals(1, schemaInstance.businessObjects.size)
        val firstBusinessObject = schemaInstance.businessObjects.first()
        assertEquals("the person business object", firstBusinessObject.name)
        val fields = firstBusinessObject.fields
        assertEquals(5, fields.size)
        assertTrue(fields[0] is NestedClazzesSchema.SingleValueFieldClazz)
        assertTrue(fields[1] is NestedClazzesSchema.SingleValueFieldClazz)
        assertTrue(fields[2] is NestedClazzesSchema.SingleValueFieldClazz)
        assertTrue(fields[3] is NestedClazzesSchema.CollectionOfValuesFieldClazz)
        assertTrue(fields[4] is NestedClazzesSchema.CollectionOfValuesFieldClazz)

        val firstnameField = fields[0] as NestedClazzesSchema.SingleValueFieldClazz
        val ageField = fields[1] as NestedClazzesSchema.SingleValueFieldClazz
        val spouseReferenceField = fields[2] as NestedClazzesSchema.SingleValueFieldClazz
        val nicknamesField = fields[3] as NestedClazzesSchema.CollectionOfValuesFieldClazz
        val childrenField = fields[4] as NestedClazzesSchema.CollectionOfValuesFieldClazz

        assertEquals("firstname", firstnameField.fieldName)
        val firstnameType = firstnameField.singleValueType as NestedClazzesSchema.BuiltinFieldTypeClazz
        assertEquals(BuiltinTypeEnum.STRING, firstnameType.builtinType)

        assertEquals("age", ageField.fieldName)
        val ageType = ageField.singleValueType as NestedClazzesSchema.BuiltinFieldTypeClazz
        assertEquals(BuiltinTypeEnum.INTEGER, ageType.builtinType)

        assertEquals("spouse", spouseReferenceField.fieldName)
        val spouseType = spouseReferenceField.singleValueType as NestedClazzesSchema.ReferenceFieldTypeClazz
        assertEquals("the person business object", spouseType.referencedBusinessObject.name)

        assertEquals("nicknames", nicknamesField.fieldName)
        assertEquals(CollectionKindEnum.LIST, nicknamesField.collectionKind)
        val nicknameType = nicknamesField.collectionValuesType as NestedClazzesSchema.BuiltinFieldTypeClazz
        assertEquals(BuiltinTypeEnum.STRING, nicknameType.builtinType)

        assertEquals("children", childrenField.fieldName)
        assertEquals(CollectionKindEnum.SET, childrenField.collectionKind)
        val childrenType = childrenField.collectionValuesType as NestedClazzesSchema.ReferenceFieldTypeClazz
        assertEquals("the person business object", childrenType.referencedBusinessObject.name)
    }
}
