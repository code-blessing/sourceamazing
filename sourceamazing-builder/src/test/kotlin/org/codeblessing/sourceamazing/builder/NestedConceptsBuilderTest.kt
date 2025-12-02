package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.NestedConceptsBuilderTest.NestedConceptsSchema.BuiltinFieldTypeConcept.BuiltinTypeEnum
import org.codeblessing.sourceamazing.builder.NestedConceptsBuilderTest.NestedConceptsSchema.CollectionOfValuesFieldConcept.CollectionKindEnum
import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.api.toConceptNameAndIdentifier
import org.codeblessing.sourceamazing.schema.withDefaultValueRootInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class NestedConceptsBuilderTest {

    private interface NestedConceptsSchema {

        val businessObjects: List<BusinessObjectConcept>

        interface BusinessObjectConcept {
            val name: String

            @References([SingleValueFieldConcept::class, CollectionOfValuesFieldConcept::class]) val fields: List<Field>
        }

        sealed interface Field {
            val fieldName: String
        }

        interface SingleValueFieldConcept : Field {
            val nullable: Boolean

            @References([BuiltinFieldTypeConcept::class, ReferenceFieldTypeConcept::class])
            val singleValueType: FieldType
        }

        interface CollectionOfValuesFieldConcept : Field {
            val collectionKind: CollectionKindEnum

            enum class CollectionKindEnum {
                LIST,
                SET,
            }

            @References([BuiltinFieldTypeConcept::class, ReferenceFieldTypeConcept::class])
            val collectionValuesType: FieldType
        }

        sealed interface FieldType

        interface BuiltinFieldTypeConcept : FieldType {
            val builtinType: BuiltinTypeEnum

            enum class BuiltinTypeEnum {
                STRING,
                INTEGER,
            }
        }

        interface ReferenceFieldTypeConcept : FieldType {
            val referencedBusinessObject: BusinessObjectConcept
        }
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(concept = NestedConceptsSchema::class, conceptAlias = "root")
    private interface NestedObjectsBuilder {

        @BuilderMethod
        @NewConcept(NestedConceptsSchema.BusinessObjectConcept::class, declareConceptAlias = "bizObj")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "businessObjects",
            referencedConceptAlias = "bizObj",
        )
        fun newBusinessObject(
            @SetConceptIdentifierValue(conceptToModifyAlias = "bizObj") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue(conceptToModifyAlias = "bizObj", facetToModify = "name") name: String,
            @InjectBuilder builder: BusinessObjectConceptBuilder.() -> Unit,
        )

        @Builder
        @ExpectedAliasFromSuperiorBuilder(
            concept = NestedConceptsSchema.BusinessObjectConcept::class,
            conceptAlias = "bizObj",
        )
        interface BusinessObjectConceptBuilder {

            @BuilderMethod
            @NewConcept(NestedConceptsSchema.SingleValueFieldConcept::class, declareConceptAlias = "field")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "field")
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "bizObj",
                facetToModify = "fields",
                referencedConceptAlias = "field",
            )
            fun addSingleValueField(
                @SetFacetValue(conceptToModifyAlias = "field", facetToModify = "fieldName") fieldName: String,
                @SetFacetValue(conceptToModifyAlias = "field", facetToModify = "nullable") nullable: Boolean = false,
                @InjectBuilder builder: FieldTypeForSingleFieldConceptBuilder.() -> Unit,
            )

            @BuilderMethod
            @NewConcept(NestedConceptsSchema.CollectionOfValuesFieldConcept::class, declareConceptAlias = "field")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "field")
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "bizObj",
                facetToModify = "fields",
                referencedConceptAlias = "field",
            )
            fun addCollectionOfValuesField(
                @SetFacetValue(conceptToModifyAlias = "field", facetToModify = "fieldName") fieldName: String,
                @SetFacetValue(conceptToModifyAlias = "field", facetToModify = "collectionKind")
                collectionKind: CollectionKindEnum,
                @InjectBuilder builder: FieldTypeForCollectionFieldConceptBuilder.() -> Unit,
            )
        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder(
            concept = NestedConceptsSchema.SingleValueFieldConcept::class,
            conceptAlias = "field",
        )
        interface FieldTypeForSingleFieldConceptBuilder {

            @BuilderMethod
            @NewConcept(NestedConceptsSchema.BuiltinFieldTypeConcept::class, declareConceptAlias = "fieldType")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "fieldType")
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "field",
                facetToModify = "singleValueType",
                referencedConceptAlias = "fieldType",
            )
            fun builtinType(
                @SetFacetValue(conceptToModifyAlias = "fieldType", facetToModify = "builtinType")
                builtinType: BuiltinTypeEnum
            )

            @BuilderMethod
            @NewConcept(NestedConceptsSchema.ReferenceFieldTypeConcept::class, declareConceptAlias = "fieldType")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "fieldType")
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "field",
                facetToModify = "singleValueType",
                referencedConceptAlias = "fieldType",
            )
            fun reference(
                @SetFacetValue(conceptToModifyAlias = "fieldType", facetToModify = "referencedBusinessObject")
                id: ConceptIdentifier
            )
        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder(
            concept = NestedConceptsSchema.CollectionOfValuesFieldConcept::class,
            conceptAlias = "field",
        )
        interface FieldTypeForCollectionFieldConceptBuilder {

            @BuilderMethod
            @NewConcept(NestedConceptsSchema.BuiltinFieldTypeConcept::class, declareConceptAlias = "fieldType")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "fieldType")
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "field",
                facetToModify = "collectionValuesType",
                referencedConceptAlias = "fieldType",
            )
            fun builtinType(
                @SetFacetValue(conceptToModifyAlias = "fieldType", facetToModify = "builtinType")
                builtinType: BuiltinTypeEnum
            )

            @BuilderMethod
            @NewConcept(NestedConceptsSchema.ReferenceFieldTypeConcept::class, declareConceptAlias = "fieldType")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "fieldType")
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "field",
                facetToModify = "collectionValuesType",
                referencedConceptAlias = "fieldType",
            )
            fun reference(
                @SetFacetValue(conceptToModifyAlias = "fieldType", facetToModify = "referencedBusinessObject")
                id: ConceptIdentifier
            )
        }
    }

    @Test
    fun `test nesting of concepts`() {
        val personBo = ConceptIdentifier.of("Person")

        val schemaInstance: NestedConceptsSchema =
            SchemaApi.withSchema(NestedConceptsSchema::class) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<NestedConceptsSchema> { conceptData ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        NestedObjectsBuilder::class,
                        mapOf("root" to conceptData.toConceptNameAndIdentifier()),
                    ) { builder ->
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
            }

        assertEquals(1, schemaInstance.businessObjects.size)
        val firstBusinessObject = schemaInstance.businessObjects.first()
        assertEquals("the person business object", firstBusinessObject.name)
        val fields = firstBusinessObject.fields
        assertEquals(5, fields.size)
        assertTrue(fields[0] is NestedConceptsSchema.SingleValueFieldConcept)
        assertTrue(fields[1] is NestedConceptsSchema.SingleValueFieldConcept)
        assertTrue(fields[2] is NestedConceptsSchema.SingleValueFieldConcept)
        assertTrue(fields[3] is NestedConceptsSchema.CollectionOfValuesFieldConcept)
        assertTrue(fields[4] is NestedConceptsSchema.CollectionOfValuesFieldConcept)

        val firstnameField = fields[0] as NestedConceptsSchema.SingleValueFieldConcept
        val ageField = fields[1] as NestedConceptsSchema.SingleValueFieldConcept
        val spouseReferenceField = fields[2] as NestedConceptsSchema.SingleValueFieldConcept
        val nicknamesField = fields[3] as NestedConceptsSchema.CollectionOfValuesFieldConcept
        val childrenField = fields[4] as NestedConceptsSchema.CollectionOfValuesFieldConcept

        assertEquals("firstname", firstnameField.fieldName)
        val firstnameType = firstnameField.singleValueType as NestedConceptsSchema.BuiltinFieldTypeConcept
        assertEquals(BuiltinTypeEnum.STRING, firstnameType.builtinType)

        assertEquals("age", ageField.fieldName)
        val ageType = ageField.singleValueType as NestedConceptsSchema.BuiltinFieldTypeConcept
        assertEquals(BuiltinTypeEnum.INTEGER, ageType.builtinType)

        assertEquals("spouse", spouseReferenceField.fieldName)
        val spouseType = spouseReferenceField.singleValueType as NestedConceptsSchema.ReferenceFieldTypeConcept
        assertEquals("the person business object", spouseType.referencedBusinessObject.name)

        assertEquals("nicknames", nicknamesField.fieldName)
        assertEquals(CollectionKindEnum.LIST, nicknamesField.collectionKind)
        val nicknameType = nicknamesField.collectionValuesType as NestedConceptsSchema.BuiltinFieldTypeConcept
        assertEquals(BuiltinTypeEnum.STRING, nicknameType.builtinType)

        assertEquals("children", childrenField.fieldName)
        assertEquals(CollectionKindEnum.SET, childrenField.collectionKind)
        val childrenType = childrenField.collectionValuesType as NestedConceptsSchema.ReferenceFieldTypeConcept
        assertEquals("the person business object", childrenType.referencedBusinessObject.name)
    }
}
