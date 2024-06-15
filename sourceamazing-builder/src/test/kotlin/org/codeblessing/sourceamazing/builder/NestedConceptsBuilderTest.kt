package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.NestedConceptsBuilderTest.NestedConceptsSchema.BuiltinFieldTypeConcept.BuiltinType
import org.codeblessing.sourceamazing.builder.NestedConceptsBuilderTest.NestedConceptsSchema.BuiltinFieldTypeConcept.BuiltinTypeEnum
import org.codeblessing.sourceamazing.builder.NestedConceptsBuilderTest.NestedConceptsSchema.BusinessObjectConcept.BusinessObjectFields
import org.codeblessing.sourceamazing.builder.NestedConceptsBuilderTest.NestedConceptsSchema.BusinessObjectConcept.BusinessObjectName
import org.codeblessing.sourceamazing.builder.NestedConceptsBuilderTest.NestedConceptsSchema.CollectionOfValuesFieldConcept.CollectionKind
import org.codeblessing.sourceamazing.builder.NestedConceptsBuilderTest.NestedConceptsSchema.CollectionOfValuesFieldConcept.CollectionKindEnum
import org.codeblessing.sourceamazing.builder.NestedConceptsBuilderTest.NestedConceptsSchema.CollectionOfValuesFieldConcept.CollectionValuesType
import org.codeblessing.sourceamazing.builder.NestedConceptsBuilderTest.NestedConceptsSchema.Field.FieldName
import org.codeblessing.sourceamazing.builder.NestedConceptsBuilderTest.NestedConceptsSchema.ReferenceFieldTypeConcept.ReferencedBusinessObject
import org.codeblessing.sourceamazing.builder.NestedConceptsBuilderTest.NestedConceptsSchema.SingleValueFieldConcept.Nullable
import org.codeblessing.sourceamazing.builder.NestedConceptsBuilderTest.NestedConceptsSchema.SingleValueFieldConcept.SingleValueType
import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NestedConceptsBuilderTest {

    @Schema(concepts = [
        NestedConceptsSchema.BusinessObjectConcept::class,
        NestedConceptsSchema.SingleValueFieldConcept::class,
        NestedConceptsSchema.CollectionOfValuesFieldConcept::class,
        NestedConceptsSchema.BuiltinFieldTypeConcept::class,
        NestedConceptsSchema.ReferenceFieldTypeConcept::class,
    ])
    private interface NestedConceptsSchema {

        @QueryConcepts(conceptClasses = [BusinessObjectConcept::class])
        fun getBusinessObjects(): List<BusinessObjectConcept>

        @Concept(facets = [
            BusinessObjectName::class,
            BusinessObjectFields::class
        ])
        interface BusinessObjectConcept {

            @StringFacet()
            interface BusinessObjectName

            @ReferenceFacet(
                minimumOccurrences = 0,
                maximumOccurrences = 100,
                referencedConcepts = [SingleValueFieldConcept::class, CollectionOfValuesFieldConcept::class]
            )
            interface BusinessObjectFields


            @QueryFacetValue(BusinessObjectName::class)
            fun name(): String


            @QueryFacetValue(BusinessObjectFields::class)
            fun fields(): List<Field>

        }

        sealed interface Field {
            @StringFacet()
            interface FieldName
            @QueryFacetValue(FieldName::class)
            fun fieldName(): String

        }

        @Concept(facets = [
            FieldName::class,
            Nullable::class,
            SingleValueType::class,
        ])
        interface SingleValueFieldConcept: Field {

            @BooleanFacet()
            interface Nullable

            @ReferenceFacet(
                minimumOccurrences = 0,
                maximumOccurrences = 100,
                referencedConcepts = [BuiltinFieldTypeConcept::class, ReferenceFieldTypeConcept::class]
            )
            interface SingleValueType

            @Suppress("UNUSED")
            @QueryFacetValue(Nullable::class)
            fun nullable(): Boolean

            @QueryFacetValue(SingleValueType::class)
            fun singleValueType(): FieldType

        }

        @Concept(facets = [
            FieldName::class,
            CollectionKind::class,
            CollectionValuesType::class,
        ])
        interface CollectionOfValuesFieldConcept: Field {
            @EnumFacet(enumerationClass = CollectionKindEnum::class)
            interface CollectionKind

            @QueryFacetValue(CollectionKind::class)
            fun collectionKind(): CollectionKindEnum

            enum class CollectionKindEnum {
                LIST, SET;
            }

            @ReferenceFacet(
                minimumOccurrences = 0,
                maximumOccurrences = 100,
                referencedConcepts = [BuiltinFieldTypeConcept::class, ReferenceFieldTypeConcept::class]
            )
            interface CollectionValuesType

            @QueryFacetValue(CollectionValuesType::class)
            fun collectionValuesType(): FieldType
        }

        sealed interface FieldType

        @Concept(facets = [
            BuiltinType::class,
        ])
        interface BuiltinFieldTypeConcept: FieldType {
            @EnumFacet(enumerationClass = BuiltinTypeEnum::class)
            interface BuiltinType

            @QueryFacetValue(BuiltinType::class)
            fun builtinType(): BuiltinTypeEnum

            enum class BuiltinTypeEnum {
                STRING, INTEGER;
            }
        }

        @Concept(facets = [
            ReferencedBusinessObject::class,
        ])
        interface ReferenceFieldTypeConcept: FieldType {

            @ReferenceFacet(referencedConcepts = [BusinessObjectConcept::class])
            interface ReferencedBusinessObject

            @QueryFacetValue(ReferencedBusinessObject::class)
            fun referencedBusinessObject(): BusinessObjectConcept
        }
    }

    @Builder
    private interface NestedObjectsBuilder {

        @BuilderMethod
        @WithNewBuilder(builderClass = BusinessObjectConceptBuilder::class)
        @NewConcept(NestedConceptsSchema.BusinessObjectConcept::class, declareConceptAlias = "bizObj")
        fun newBusinessObject(
            @SetConceptIdentifierValue(conceptToModifyAlias = "bizObj") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue(conceptToModifyAlias = "bizObj", facetToModify = BusinessObjectName::class) name: String,
            @InjectBuilder builder: BusinessObjectConceptBuilder.() -> Unit
        )


        @Builder
        @ExpectedAliasFromSuperiorBuilder("bizObj")
        interface BusinessObjectConceptBuilder {

            @BuilderMethod
            @WithNewBuilder(builderClass = FieldTypeForSingleFieldConceptBuilder::class)
            @NewConcept(NestedConceptsSchema.SingleValueFieldConcept::class, declareConceptAlias = "field")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "field")
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "bizObj", facetToModify = BusinessObjectFields::class, referencedConceptAlias = "field")
            fun addSingleValueField(
                @SetFacetValue(conceptToModifyAlias = "field", facetToModify = FieldName::class) fieldName: String,
                @SetFacetValue(conceptToModifyAlias = "field", facetToModify = Nullable::class) nullable: Boolean = false,
                @InjectBuilder builder: FieldTypeForSingleFieldConceptBuilder.() -> Unit,
            )

            @BuilderMethod
            @WithNewBuilder(builderClass = FieldTypeForCollectionFieldConceptBuilder::class)
            @NewConcept(NestedConceptsSchema.CollectionOfValuesFieldConcept::class, declareConceptAlias = "field")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "field")
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "bizObj", facetToModify = BusinessObjectFields::class, referencedConceptAlias = "field")
            fun addCollectionOfValuesField(
                @SetFacetValue(conceptToModifyAlias = "field", facetToModify = FieldName::class) fieldName: String,
                @SetFacetValue(conceptToModifyAlias = "field", facetToModify = CollectionKind::class) collectionKind: CollectionKindEnum,
                @InjectBuilder builder: FieldTypeForCollectionFieldConceptBuilder.() -> Unit,
            )

        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder("field")
        interface FieldTypeForSingleFieldConceptBuilder {

            @BuilderMethod
            @NewConcept(NestedConceptsSchema.BuiltinFieldTypeConcept::class, declareConceptAlias = "fieldType")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "fieldType")
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "field", facetToModify = SingleValueType::class, referencedConceptAlias = "fieldType")
            fun builtinType(
                @SetFacetValue(conceptToModifyAlias = "fieldType", facetToModify = BuiltinType::class) builtinType: BuiltinTypeEnum,
            )

            @BuilderMethod
            @NewConcept(NestedConceptsSchema.ReferenceFieldTypeConcept::class, declareConceptAlias = "fieldType")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "fieldType")
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "field", facetToModify = SingleValueType::class, referencedConceptAlias = "fieldType")
            fun reference(
                @SetFacetValue(conceptToModifyAlias = "fieldType", facetToModify = ReferencedBusinessObject::class) id: ConceptIdentifier,
            )
        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder("field")
        interface FieldTypeForCollectionFieldConceptBuilder {

            @BuilderMethod
            @NewConcept(NestedConceptsSchema.BuiltinFieldTypeConcept::class, declareConceptAlias = "fieldType")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "fieldType")
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "field", facetToModify = CollectionValuesType::class, referencedConceptAlias = "fieldType")
            fun builtinType(
                @SetFacetValue(conceptToModifyAlias = "fieldType", facetToModify = BuiltinType::class) builtinType: BuiltinTypeEnum,
            )

            @BuilderMethod
            @NewConcept(NestedConceptsSchema.ReferenceFieldTypeConcept::class, declareConceptAlias = "fieldType")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "fieldType")
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "field", facetToModify = CollectionValuesType::class, referencedConceptAlias = "fieldType")
            fun reference(
                @SetFacetValue(conceptToModifyAlias = "fieldType", facetToModify = ReferencedBusinessObject::class) id: ConceptIdentifier,
            )
        }
    }

    @Test
    fun `test nesting of concepts`() {
        val personBo =  ConceptIdentifier.of("Person")

        val schemaInstance: NestedConceptsSchema = SchemaApi.withSchema(schemaDefinitionClass = NestedConceptsSchema::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, NestedObjectsBuilder::class) { builder ->
                builder.newBusinessObject(personBo, "the person business object") {
                    addSingleValueField("firstname") {
                        builtinType(BuiltinTypeEnum.STRING)
                    }

                    addSingleValueField("age") {
                        builtinType(BuiltinTypeEnum.INTEGER)
                    }
                    addSingleValueField("spouse") {
                        reference(personBo)
                    }
                    addCollectionOfValuesField("nicknames", CollectionKindEnum.LIST) {
                        builtinType(BuiltinTypeEnum.STRING)
                    }
                    addCollectionOfValuesField("children", CollectionKindEnum.SET) {
                        reference(personBo)
                    }
                }

            }
        }

        assertEquals(1, schemaInstance.getBusinessObjects().size)
        val firstBusinessObject = schemaInstance.getBusinessObjects().first()
        assertEquals("the person business object", firstBusinessObject.name())
        val fields = firstBusinessObject.fields()
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


        assertEquals("firstname", firstnameField.fieldName())
        val firstnameType = firstnameField.singleValueType() as NestedConceptsSchema.BuiltinFieldTypeConcept
        assertEquals(BuiltinTypeEnum.STRING, firstnameType.builtinType())

        assertEquals("age", ageField.fieldName())
        val ageType = ageField.singleValueType() as NestedConceptsSchema.BuiltinFieldTypeConcept
        assertEquals(BuiltinTypeEnum.INTEGER, ageType.builtinType())

        assertEquals("spouse", spouseReferenceField.fieldName())
        val spouseType = spouseReferenceField.singleValueType() as NestedConceptsSchema.ReferenceFieldTypeConcept
        assertEquals("the person business object", spouseType.referencedBusinessObject().name())

        assertEquals("nicknames", nicknamesField.fieldName())
        assertEquals(CollectionKindEnum.LIST, nicknamesField.collectionKind())
        val nicknameType = nicknamesField.collectionValuesType() as NestedConceptsSchema.BuiltinFieldTypeConcept
        assertEquals(BuiltinTypeEnum.STRING, nicknameType.builtinType())

        assertEquals("children", childrenField.fieldName())
        assertEquals(CollectionKindEnum.SET, childrenField.collectionKind())
        val childrenType = childrenField.collectionValuesType() as NestedConceptsSchema.ReferenceFieldTypeConcept
        assertEquals("the person business object", childrenType.referencedBusinessObject().name())
    }
}