package org.codeblessing.sourceamazing.engine.process

import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.DomainUnit
import org.codeblessing.sourceamazing.api.process.datacollection.builder.annotations.*
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.api.process.schema.annotations.FacetType
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConcepts
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryFacet
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector
import org.codeblessing.sourceamazing.engine.process.NestedConceptsTest.NestedConceptsSchema.BuiltinFieldTypeConcept.BuiltinType
import org.codeblessing.sourceamazing.engine.process.NestedConceptsTest.NestedConceptsSchema.BuiltinFieldTypeConcept.BuiltinTypeEnum
import org.codeblessing.sourceamazing.engine.process.NestedConceptsTest.NestedConceptsSchema.BusinessObjectConcept.BusinessObjectFields
import org.codeblessing.sourceamazing.engine.process.NestedConceptsTest.NestedConceptsSchema.BusinessObjectConcept.BusinessObjectName
import org.codeblessing.sourceamazing.engine.process.NestedConceptsTest.NestedConceptsSchema.CollectionOfValuesFieldConcept.*
import org.codeblessing.sourceamazing.engine.process.NestedConceptsTest.NestedConceptsSchema.Field.FieldName
import org.codeblessing.sourceamazing.engine.process.NestedConceptsTest.NestedConceptsSchema.ReferenceFieldTypeConcept.ReferencedBusinessObject
import org.codeblessing.sourceamazing.engine.process.NestedConceptsTest.NestedConceptsSchema.SingleValueFieldConcept.Nullable
import org.codeblessing.sourceamazing.engine.process.NestedConceptsTest.NestedConceptsSchema.SingleValueFieldConcept.SingleValueType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NestedConceptsTest {

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

            @Facet(FacetType.TEXT)
            interface BusinessObjectName

            @Facet(
                FacetType.REFERENCE,
                minimumOccurrences = 0,
                maximumOccurrences = 100,
                referencedConcepts = [SingleValueFieldConcept::class, CollectionOfValuesFieldConcept::class]
            )
            interface BusinessObjectFields


            @QueryFacet(BusinessObjectName::class)
            fun name(): String


            @QueryFacet(BusinessObjectFields::class)
            fun fields(): List<Field>

        }

        sealed interface Field {
            @Facet(FacetType.TEXT)
            interface FieldName
            @QueryFacet(FieldName::class)
            fun fieldName(): String

        }

        @Concept(facets = [
            FieldName::class,
            Nullable::class,
            SingleValueType::class,
        ])
        interface SingleValueFieldConcept: Field {

            @Facet(FacetType.BOOLEAN)
            interface Nullable

            @Facet(
                FacetType.REFERENCE,
                minimumOccurrences = 0,
                maximumOccurrences = 100,
                referencedConcepts = [BuiltinFieldTypeConcept::class, ReferenceFieldTypeConcept::class]
            )
            interface SingleValueType

            @QueryFacet(Nullable::class)
            fun nullable(): Boolean

            @QueryFacet(SingleValueType::class)
            fun singleValueType(): FieldType

        }

        @Concept(facets = [
            FieldName::class,
            CollectionKind::class,
            CollectionValuesType::class,
        ])
        interface CollectionOfValuesFieldConcept: Field {
            @Facet(FacetType.TEXT_ENUMERATION, enumerationClass = CollectionKindEnum::class)
            interface CollectionKind

            @QueryFacet(CollectionKind::class)
            fun collectionKind(): CollectionKindEnum

            enum class CollectionKindEnum {
                LIST, SET;
            }

            @Facet(
                FacetType.REFERENCE,
                minimumOccurrences = 0,
                maximumOccurrences = 100,
                referencedConcepts = [BuiltinFieldTypeConcept::class, ReferenceFieldTypeConcept::class]
            )
            interface CollectionValuesType

            @QueryFacet(CollectionValuesType::class)
            fun collectionValuesType(): FieldType
        }

        sealed interface FieldType

        @Concept(facets = [
            BuiltinType::class,
        ])
        interface BuiltinFieldTypeConcept: FieldType {
            @Facet(FacetType.TEXT_ENUMERATION, enumerationClass = BuiltinTypeEnum::class)
            interface BuiltinType

            @QueryFacet(BuiltinType::class)
            fun builtinType(): BuiltinTypeEnum

            enum class BuiltinTypeEnum {
                STRING, INTEGER;
            }
        }

        @Concept(facets = [
            ReferencedBusinessObject::class,
        ])
        interface ReferenceFieldTypeConcept: FieldType {

            @Facet(FacetType.REFERENCE, referencedConcepts = [BusinessObjectConcept::class])
            interface ReferencedBusinessObject

            @QueryFacet(ReferencedBusinessObject::class)
            fun referencedBusinessObject(): BusinessObjectConcept
        }
    }

    @Builder
    private interface NestedObjectsDataCollector {

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
            @SetRandomConceptIdentifier(conceptToModifyAlias = "field")
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "bizObj", facetToModify = BusinessObjectFields::class, referencedConceptAlias = "field")
            fun addSingleValueField(
                @SetFacetValue(conceptToModifyAlias = "field", facetToModify = FieldName::class) fieldName: String,
                @SetFacetValue(conceptToModifyAlias = "field", facetToModify = Nullable::class) nullable: Boolean = false,
                @InjectBuilder builder: FieldTypeForSingleFieldConceptBuilder.() -> Unit,
            )

            @BuilderMethod
            @WithNewBuilder(builderClass = FieldTypeForCollectionFieldConceptBuilder::class)
            @NewConcept(NestedConceptsSchema.CollectionOfValuesFieldConcept::class, declareConceptAlias = "field")
            @SetRandomConceptIdentifier(conceptToModifyAlias = "field")
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
            @SetRandomConceptIdentifier(conceptToModifyAlias = "fieldType")
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "field", facetToModify = SingleValueType::class, referencedConceptAlias = "fieldType")
            fun builtinType(
                @SetFacetValue(conceptToModifyAlias = "fieldType", facetToModify = BuiltinType::class) builtinType: BuiltinTypeEnum,
            )

            @BuilderMethod
            @NewConcept(NestedConceptsSchema.ReferenceFieldTypeConcept::class, declareConceptAlias = "fieldType")
            @SetRandomConceptIdentifier(conceptToModifyAlias = "fieldType")
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
            @SetRandomConceptIdentifier(conceptToModifyAlias = "fieldType")
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "field", facetToModify = CollectionValuesType::class, referencedConceptAlias = "fieldType")
            fun builtinType(
                @SetFacetValue(conceptToModifyAlias = "fieldType", facetToModify = BuiltinType::class) builtinType: BuiltinTypeEnum,
            )

            @BuilderMethod
            @NewConcept(NestedConceptsSchema.ReferenceFieldTypeConcept::class, declareConceptAlias = "fieldType")
            @SetRandomConceptIdentifier(conceptToModifyAlias = "fieldType")
            @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "field", facetToModify = CollectionValuesType::class, referencedConceptAlias = "fieldType")
            fun reference(
                @SetFacetValue(conceptToModifyAlias = "fieldType", facetToModify = ReferencedBusinessObject::class) id: ConceptIdentifier,
            )
        }
    }

    private class NestedObjectsDomainUnit: DomainUnit<NestedConceptsSchema, NestedObjectsDataCollector>(
        schemaDefinitionClass = NestedConceptsSchema::class,
        inputDefinitionClass = NestedObjectsDataCollector::class
    ) {
        private val personBo =  ConceptIdentifier.of("Person")

        override fun collectInputData(
            parameterAccess: ParameterAccess,
            extensionAccess: DataCollectionExtensionAccess,
            dataCollector: NestedObjectsDataCollector
        ) {
            dataCollector.newBusinessObject(personBo, "the person business object") {
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

        override fun collectTargetFiles(
            parameterAccess: ParameterAccess,
            schemaInstance: NestedConceptsSchema,
            targetFilesCollector: TargetFilesCollector
        ) {
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

    @Test
    fun `test nesting of concepts`() {
        val testProcessSession = ProcessSession(domainUnits = listOf(NestedObjectsDomainUnit()))
        val engineProcess = EngineProcess(testProcessSession)

        engineProcess.runProcess()
    }
}