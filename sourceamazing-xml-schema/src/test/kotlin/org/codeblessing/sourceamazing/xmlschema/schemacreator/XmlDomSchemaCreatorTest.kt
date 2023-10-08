package org.codeblessing.sourceamazing.xmlschema.schemacreator

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.ConceptSchema
import org.codeblessing.sourceamazing.api.process.schema.FacetTypeEnum
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.engine.process.schema.FacetSchemaImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class XmlDomSchemaCreatorTest {

    private val expectedXml = """
        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <xsd:schema xmlns="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema" elementFormDefault="qualified" targetNamespace="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <!-- - - - - - - - -       CONCEPT IDENTIFIER ATTRIBUTE     - - - - - - - -->
            <xsd:attributeGroup name="conceptIdentifier">
                <xsd:attribute name="conceptIdentifier" type="xsd:ID"/>
            </xsd:attributeGroup>
            <!-- - - - - - - - -       CONFIGURATION AND DEFINITIONS     - - - - - - - -->
            <xsd:element name="sourceamazing">
                <xsd:complexType>
                    <xsd:sequence maxOccurs="1" minOccurs="1">
                        <xsd:element name="definitions">
                            <xsd:complexType>
                                <!-- - - - - - - - -       ROOT CONCEPTS     - - - - - - - -->
                                <xsd:choice maxOccurs="unbounded" minOccurs="0">
                                    <xsd:element name="testEntity" type="testEntityType"/>
                                </xsd:choice>
                            </xsd:complexType>
                        </xsd:element>
                    </xsd:sequence>
                </xsd:complexType>
            </xsd:element>
            <!-- - - - - - - - -       ALL CONCEPTS AS TYPES     - - - - - - - -->
            <xsd:complexType name="testEntityType">
                <xsd:choice maxOccurs="unbounded" minOccurs="0">
                    <xsd:element name="testEntityAttribute" type="testEntityAttributeType"/>
                </xsd:choice>
                <xsd:attributeGroup ref="conceptIdentifier"/>
                <xsd:attributeGroup ref="testEntityTestEntityName"/>
            </xsd:complexType>
            <xsd:complexType name="testEntityAttributeType">
                <xsd:choice maxOccurs="unbounded" minOccurs="0"/>
                <xsd:attributeGroup ref="conceptIdentifier"/>
                <xsd:attributeGroup ref="testEntityAttributeTestEntityAttributeName"/>
            </xsd:complexType>
            <!-- - - - - - - - -       ALL ATTRIBUTES      - - - - - - - -->
            <xsd:attributeGroup name="testEntityTestEntityName">
                <xsd:attribute name="testEntityName" type="xsd:string"/>
            </xsd:attributeGroup>
            <xsd:attributeGroup name="testEntityAttributeTestEntityAttributeName">
                <xsd:attribute name="testEntityAttributeName" type="xsd:string"/>
            </xsd:attributeGroup>
        </xsd:schema>

    """.trimIndent()


    @Test
    fun testXmlDomSchemaCreator() {
        val schema: SchemaAccess = createSchema()
        val schemaContent = XmlDomSchemaCreator.createSchemagicSchemaContent(schema)
        assertEquals(expectedXml, schemaContent)
    }

    private val testEntityConceptName = ConceptName.of("TestEntity")
    private val testEntityNameFacetName = FacetName.of("TestEntityName")

    private val testEntityAttributeConceptName = ConceptName.of("TestEntityAttribute")
    private val testEntityAttributeNameFacetName = FacetName.of("TestEntityAttributeName")

    private fun createSchema(): SchemaAccess {
        val testEntityConcept: ConceptSchema = SimpleConceptSchema(
            conceptName = testEntityConceptName,
            conceptClass = SimpleConceptSchema::class.java, // TODO This is the wrong interface
            parentConceptName = null,
            facets = listOf(
                FacetSchemaImpl(testEntityNameFacetName, FacetTypeEnum.TEXT, mandatory = true, referencingConcept = null, enumerationType = null),
            )
        )

        val testEntityAttributeConcept: ConceptSchema = SimpleConceptSchema(
            conceptName = testEntityAttributeConceptName,
            conceptClass = SimpleConceptSchema::class.java, // TODO This is the wrong interface
            parentConceptName = testEntityConceptName,
            facets = listOf(
                FacetSchemaImpl(testEntityAttributeNameFacetName, FacetTypeEnum.TEXT, mandatory = true, referencingConcept = null, enumerationType = null),
            )
        )

        return SimpleSchema(listOf(testEntityConcept, testEntityAttributeConcept))
    }

}
