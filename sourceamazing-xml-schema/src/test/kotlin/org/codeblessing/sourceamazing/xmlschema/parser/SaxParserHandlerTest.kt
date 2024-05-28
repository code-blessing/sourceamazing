package org.codeblessing.sourceamazing.xmlschema.parser

import org.codeblessing.sourceamazing.schema.filesystem.PhysicalFilesFileSystemAccess
import org.codeblessing.sourceamazing.schema.logger.JavaUtilLoggerFacade
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.xmlschema.XmlTestSchema
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

internal class SaxParserHandlerTest {


    private val testXml = """
        <?xml version="1.0" encoding="utf-8" ?>
        <sourceamazing xmlns="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:schemaLocation="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema ./schema/sourceamazing-xml-schema.xsd">
            <definitions>
                <testEntityConcept conceptIdentifier="Person" name="Person" kotlinModelClassname="FirstTest" kotlinModelPackage="org.codeblessing.sourceamazing.entities">
                    <testEntityAttribute>
                        <testEntityAttributeConcept conceptIdentifier="Firstname" name="firstname" type="TEXT" />
                        <testEntityAttributeConcept conceptIdentifier="Lastname" name="lastname" type="TEXT" />
                    </testEntityAttribute>
                </testEntityConcept>
                <testEntityConcept conceptIdentifier="Address" name="Address" kotlinModelClassname="Address" kotlinModelPackage="org.codeblessing.sourceamazing.entities">
                    <testEntityAttribute>
                        <testEntityAttributeConcept conceptIdentifier="Street" name="street" type="TEXT" />
                        <testEntityAttributeConcept conceptIdentifier="Zip" name="zip" type="NUMBER" />
                    </testEntityAttribute>
                </testEntityConcept>
            </definitions>
        </sourceamazing>
    """.trimIndent()

    @Test
    fun testSaxParser() {
        val virtualFileSystem = PhysicalFilesFileSystemAccess()
        val logger = JavaUtilLoggerFacade(virtualFileSystem)
        val factory: SAXParserFactory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = true
        factory.isValidating = false // turn of validation as schema is not found
        val saxParser: SAXParser = factory.newSAXParser()
        val dataCollector = ConceptDataCollector()

        val schema = createSchema()
        val saxParserHandler = SaxParserHandler(schema, dataCollector, emptyMap(), Paths.get("."), virtualFileSystem, logger)

        testXml.byteInputStream().use {
            saxParser.parse(it, saxParserHandler)
        }

        val conceptDataList = dataCollector.provideConceptData()

        assertEquals(6, conceptDataList.size)

        val personRootNode = conceptDataList[0]
        assertEquals(XmlTestSchema.testEntityConceptName, personRootNode.conceptName)
        assertEquals("Person", personRootNode.getFacet(XmlTestSchema.testEntityNameFacetName).firstOrNull())
        assertEquals("FirstTest", personRootNode.getFacet(XmlTestSchema.testEntityKotlinModelClassnameFacetName).firstOrNull())
        assertEquals("org.codeblessing.sourceamazing.entities", personRootNode.getFacet(XmlTestSchema.testEntityKotlinModelPackageFacetName).firstOrNull())
        val firstnameNode = conceptDataList[1]
        assertEquals(XmlTestSchema.testEntityAttributeConceptName, firstnameNode.conceptName)
        assertEquals("firstname", firstnameNode.getFacet(XmlTestSchema.testEntityAttributeNameFacetName).firstOrNull())
        assertEquals(XmlTestSchema.TestEntityAttributeConcept.AttributeTypeEnum.TEXT, firstnameNode.getFacet(XmlTestSchema.testEntityAttributeTypeFacetName).firstOrNull())
        val addressRootNode = conceptDataList[3]
        assertEquals(XmlTestSchema.testEntityConceptName, addressRootNode.conceptName)
        assertEquals("Address", addressRootNode.getFacet(XmlTestSchema.testEntityNameFacetName).firstOrNull())
        assertEquals("org.codeblessing.sourceamazing.entities", addressRootNode.getFacet(XmlTestSchema.testEntityKotlinModelPackageFacetName).firstOrNull())
    }

    private fun createSchema(): SchemaAccess {
        return XmlTestSchema.createSchema()
    }
}

