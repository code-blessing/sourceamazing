package org.codeblessing.sourceamazing.xmlschema.xsdcreator

import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.xmlschema.XmlTestSchema
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class XmlDomSchemaCreatorTest {

    private val expectedXml = """
        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <xsd:schema xmlns="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema" elementFormDefault="qualified" targetNamespace="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <!-- - - - - - - - -       CONCEPT IDENTIFIER ATTRIBUTE     - - - - - - - -->
            <xsd:attributeGroup name="conceptIdentifierAttributeGroup">
                <xsd:attribute name="conceptIdentifier" type="xsd:ID"/>
            </xsd:attributeGroup>
            <xsd:element name="conceptRef">
                <xsd:complexType>
                    <xsd:attribute name="conceptIdentifierReference" type="xsd:IDREF"/>
                </xsd:complexType>
            </xsd:element>
            <!-- - - - - - - - -       DEFINITIONS     - - - - - - - -->
            <xsd:element name="sourceamazing">
                <xsd:complexType>
                    <xsd:sequence>
                        <xsd:element name="definitions">
                            <xsd:complexType>
                                <!-- - - - - - - - -       CONCEPTS     - - - - - - - -->
                                <xsd:choice maxOccurs="unbounded" minOccurs="0">
                                    <xsd:element ref="testEntityConcept"/>
                                    <xsd:element ref="testEntityAttributeConcept"/>
                                </xsd:choice>
                            </xsd:complexType>
                        </xsd:element>
                    </xsd:sequence>
                </xsd:complexType>
            </xsd:element>
            <!-- - - - - - - - -       ALL CONCEPTS AS TYPES     - - - - - - - -->
            <!-- - - - - - - - -       Concept: testEntityConcept     - - - - - - - -->
            <xsd:element name="testEntityConcept" type="TestEntityConceptType"/>
            <xsd:complexType name="TestEntityConceptType">
                <xsd:all>
                    <!-- - - - - - - - -       Facet: TestEntityAttribute     - - - - - - - -->
                    <xsd:element maxOccurs="1" minOccurs="0" name="testEntityAttribute">
                        <xsd:complexType>
                            <xsd:sequence maxOccurs="10" minOccurs="0">
                                <xsd:choice maxOccurs="1" minOccurs="1">
                                    <xsd:element name="testEntityAttributeConcept" type="TestEntityAttributeConceptType"/>
                                    <xsd:element ref="conceptRef"/>
                                </xsd:choice>
                            </xsd:sequence>
                        </xsd:complexType>
                    </xsd:element>
                </xsd:all>
                <xsd:attributeGroup ref="conceptIdentifierAttributeGroup"/>
                <!-- - - - - - - - -       Facet: Name     - - - - - - - -->
                <xsd:attribute name="name" type="xsd:string" use="required"/>
                <!-- - - - - - - - -       Facet: KotlinModelClassname     - - - - - - - -->
                <xsd:attribute name="kotlinModelClassname" type="xsd:string" use="required"/>
                <!-- - - - - - - - -       Facet: KotlinModelPackage     - - - - - - - -->
                <xsd:attribute name="kotlinModelPackage" type="xsd:string" use="required"/>
            </xsd:complexType>
            <!-- - - - - - - - -       Concept: testEntityAttributeConcept     - - - - - - - -->
            <xsd:element name="testEntityAttributeConcept" type="TestEntityAttributeConceptType"/>
            <xsd:complexType name="TestEntityAttributeConceptType">
                <xsd:attributeGroup ref="conceptIdentifierAttributeGroup"/>
                <!-- - - - - - - - -       Facet: Name     - - - - - - - -->
                <xsd:attribute name="name" type="xsd:string" use="required"/>
                <!-- - - - - - - - -       Facet: Type     - - - - - - - -->
                <xsd:attribute name="type" use="required">
                    <xsd:simpleType>
                        <xsd:restriction base="xsd:string">
                            <xsd:enumeration value="TEXT"/>
                            <xsd:enumeration value="NUMBER"/>
                            <xsd:enumeration value="BOOLEAN"/>
                        </xsd:restriction>
                    </xsd:simpleType>
                </xsd:attribute>
            </xsd:complexType>
        </xsd:schema>

    """.trimIndent()


    @Test
    fun testXmlDomSchemaCreator() {
        val schema: SchemaAccess = XmlTestSchema.createSchema()
        val schemaContent = XmlDomSchemaCreator.createXsdSchemaContent(schema)
        assertEquals(expectedXml, schemaContent)
    }
}
