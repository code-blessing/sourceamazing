package org.codeblessing.sourceamazing.engine.process

import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.DomainUnit
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.annotations.*
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector
import org.codeblessing.sourceamazing.engine.process.ConceptReuseTest.ConceptReuseSchema.BuiltinFieldTypeConcept.BuiltinTypeEnum
import org.codeblessing.sourceamazing.engine.process.ConceptReuseTest.ConceptReuseSchema.CollectionOfValuesFieldConcept.CollectionKindEnum
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConceptReuseTest {

    @Schema
    private interface ConceptReuseSchema {

        @ChildConcepts(BusinessObjectConcept::class)
        fun getBusinessObjects(): List<BusinessObjectConcept>

        @Concept("BusinessObjectConcept")
        interface BusinessObjectConcept {

            @Facet("BusinessObjectName", mandatory = true)
            fun name(): String


            @ChildConceptsWithCommonBaseInterface(
                baseInterfaceClass = Field::class,
                conceptClasses = [
                    SingleValueFieldConcept::class,
                    CollectionOfValuesFieldConcept::class,
                ]
            )
            fun fields(): List<Field>

        }

        sealed interface Field {
            @Facet("FieldName")
            fun fieldName(): String

        }

        @Concept("SingleValueFieldConcept")
        interface SingleValueFieldConcept: Field {

            @Facet("Nullable")
            fun nullable(): Boolean

            @ChildConceptsWithCommonBaseInterface(
                baseInterfaceClass = FieldType::class,
                conceptClasses = [
                    BuiltinFieldTypeConcept::class,
                    ReferenceFieldTypeConcept::class,
                ]
            )
            fun singleValueType(): List<FieldType> // TODO unfortunately a list, not a single concept

        }

        @Concept("CollectionOfValuesFieldConcept")
        interface CollectionOfValuesFieldConcept: Field {

            @Facet("CollectionKind")
            fun collectionKind(): CollectionKindEnum

            enum class CollectionKindEnum {
                LIST, SET;
            }

            @ChildConceptsWithCommonBaseInterface(
                baseInterfaceClass = FieldType::class,
                conceptClasses = [
                    BuiltinFieldTypeConcept::class,
                    ReferenceFieldTypeConcept::class,
                ]
            )
            fun collectionValuesType(): List<FieldType> // TODO unfortunately a list, not a single concept
        }

        sealed interface FieldType

        @Concept("BuiltinFieldTypeConcept")
        interface BuiltinFieldTypeConcept: FieldType {

            @Facet("BuiltinType")
            fun builtinType(): BuiltinTypeEnum

            enum class BuiltinTypeEnum {
                STRING, INTEGER;
            }
        }

        @Concept("ReferenceFieldTypeConcept")
        interface ReferenceFieldTypeConcept: FieldType {

            @Facet("ReferencedBusinessObject")
            fun referencedBusinessObject(): BusinessObjectConcept
        }
    }

    @DataCollector
    private interface TestInputDefinition {

        @AddConceptAndFacets(conceptBuilderClazz = BusinessObjectConceptBuilder::class)
        @ConceptNameValue("BusinessObjectConcept")
        fun newBusinessObject(
            @ConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @FacetValue("BusinessObjectName") name: String,
            @ConceptBuilder builder: BusinessObjectConceptBuilder.() -> Unit
        )


        interface BusinessObjectConceptBuilder {

            @AddConceptAndFacets(conceptBuilderClazz = FieldTypeConceptBuilder::class)
            @ConceptNameValue("SingleValueFieldConcept")
            @AutoRandomConceptIdentifier
            fun addSingleValueField(
                @FacetValue("FieldName") fieldName: String,
                @FacetValue("Nullable") nullable: Boolean = false,
                @ConceptBuilder builder: FieldTypeConceptBuilder.() -> Unit,
            )

            @AddConceptAndFacets(conceptBuilderClazz = FieldTypeConceptBuilder::class)
            @ConceptNameValue("CollectionOfValuesFieldConcept")
            @AutoRandomConceptIdentifier
            fun addCollectionOfValuesField(
                @FacetValue("FieldName") fieldName: String,
                @FacetValue("CollectionKind") collectionKind: CollectionKindEnum,
                @ConceptBuilder builder: FieldTypeConceptBuilder.() -> Unit,
            )

        }

        interface FieldTypeConceptBuilder {

            @AddConceptAndFacets(conceptBuilderClazz = EmptyBuilder::class)
            @ConceptNameValue("BuiltinFieldTypeConcept")
            @AutoRandomConceptIdentifier
            fun builtinType(
                @FacetValue("BuiltinType") builtinType: BuiltinTypeEnum,
            )

            @AddConceptAndFacets(conceptBuilderClazz = EmptyBuilder::class)
            @ConceptNameValue("ReferenceFieldTypeConcept")
            @AutoRandomConceptIdentifier
            fun reference(
                @FacetValue("ReferencedBusinessObject") id: ConceptIdentifier,
            )

        }

        interface EmptyBuilder
    }

    private class TestDomainUnit: DomainUnit<ConceptReuseSchema, TestInputDefinition>(
        schemaDefinitionClass = ConceptReuseSchema::class.java,
        inputDefinitionClass = TestInputDefinition::class.java
    ) {
        private val personBo =  ConceptIdentifier.of("Person")

        override fun collectInputData(
            parameterAccess: ParameterAccess,
            extensionAccess: DataCollectionExtensionAccess,
            dataCollector: TestInputDefinition
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
            schemaInstance: ConceptReuseSchema,
            targetFilesCollector: TargetFilesCollector
        ) {
            assertEquals(1, schemaInstance.getBusinessObjects().size)
            val firstBusinessObject = schemaInstance.getBusinessObjects().first()
            assertEquals("the person business object", firstBusinessObject.name())
            assertEquals(5, firstBusinessObject.fields().size)
            val firstnameField = firstBusinessObject.fields()[0] as ConceptReuseSchema.SingleValueFieldConcept
            val ageField = firstBusinessObject.fields()[1] as ConceptReuseSchema.SingleValueFieldConcept
            val spouseReferenceField = firstBusinessObject.fields()[2] as ConceptReuseSchema.SingleValueFieldConcept
            val nicknamesField = firstBusinessObject.fields()[3] as ConceptReuseSchema.CollectionOfValuesFieldConcept
            val childrenField = firstBusinessObject.fields()[4] as ConceptReuseSchema.CollectionOfValuesFieldConcept


            assertEquals("firstname", firstnameField.fieldName())
            val firstnameType = firstnameField.singleValueType().first() as ConceptReuseSchema.BuiltinFieldTypeConcept
            assertEquals(BuiltinTypeEnum.STRING, firstnameType.builtinType())

            assertEquals("age", ageField.fieldName())
            val ageType = ageField.singleValueType().first() as ConceptReuseSchema.BuiltinFieldTypeConcept
            assertEquals(BuiltinTypeEnum.INTEGER, ageType.builtinType())

            assertEquals("spouse", spouseReferenceField.fieldName())
            val spouseType = spouseReferenceField.singleValueType().first() as ConceptReuseSchema.ReferenceFieldTypeConcept
            assertEquals("the person business object", spouseType.referencedBusinessObject().name())

            assertEquals("nicknames", nicknamesField.fieldName())
            assertEquals(CollectionKindEnum.LIST, nicknamesField.collectionKind())
            val nicknameType = nicknamesField.collectionValuesType().first() as ConceptReuseSchema.BuiltinFieldTypeConcept
            assertEquals(BuiltinTypeEnum.STRING, nicknameType.builtinType())

            assertEquals("children", childrenField.fieldName())
            assertEquals(CollectionKindEnum.SET, childrenField.collectionKind())
            val childrenType = childrenField.collectionValuesType().first() as ConceptReuseSchema.ReferenceFieldTypeConcept
            assertEquals("the person business object", childrenType.referencedBusinessObject().name())

        }
    }

    @Test
    fun `test reuse of a concept in different parent concepts`() {
        val testProcessSession = ProcessSession(domainUnits = listOf(TestDomainUnit()))
        val engineProcess = EngineProcess(testProcessSession)

        engineProcess.runProcess()
    }
}