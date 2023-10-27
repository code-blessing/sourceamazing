package org.codeblessing.sourceamazing.xmlschema.parser

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcepts
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.engine.process.schema.SchemaCreator
import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.filesystem.PhysicalFilesFileSystemAccess
import org.codeblessing.sourceamazing.engine.logger.JavaUtilLoggerFacade
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

private const val testEntityConceptNameConst = "TestEntity"
private const val testEntityNameFacetNameConst = "TestEntityName"
private const val testEntityKotlinModelClassnameFacetNameConst = "TestKotlinModelClassname"
private const val testEntityKotlinModelPackageFacetNameConst = "TestKotlinModelPackage"
private const val testEntityAttributeConceptNameConst = "TestEntityAttribute"
private const val testEntityAttributeNameFacetNameConst = "TestEntityAttributeName"
private const val testEntityAttributeTypeFacetNameConst = "TestEntityAttributeType"
private const val testKotlinFieldTypeFacetNameConst = "TestKotlinFieldType"

internal class SaxParserHandlerTest {

    private val testEntityConceptName = ConceptName.of(testEntityConceptNameConst)
    private val testEntityNameFacetName = FacetName.of(testEntityNameFacetNameConst)
    private val testEntityKotlinModelClassnameFacetName = FacetName.of(testEntityKotlinModelClassnameFacetNameConst)
    private val testEntityKotlinModelPackageFacetName = FacetName.of(testEntityKotlinModelPackageFacetNameConst)
    private val testEntityAttributeConceptName = ConceptName.of(testEntityAttributeConceptNameConst)
    private val testEntityAttributeNameFacetName = FacetName.of(testEntityAttributeNameFacetNameConst)
    private val testEntityAttributeTypeFacetName = FacetName.of(testEntityAttributeTypeFacetNameConst)
    private val testKotlinFieldTypeFacetName = FacetName.of(testKotlinFieldTypeFacetNameConst)

    private val testXml = """
        <?xml version="1.0" encoding="utf-8" ?>
        <sourceamazing xmlns="https://codeblessing.org/sourceamazing"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="https://codeblessing.org/sourceamazing sourceamazing-schema.xsd">
            <configuration testKotlinModelPackage="org.codeblessing.sourceamazing.entities"/>
            <definitions>
                <testEntity testEntityName="Person" testKotlinModelClassname="Person">
                    <testEntityAttribute testEntityAttributeName="firstname" testEntityAttributeType="TEXT" testKotlinFieldType="kotlin.String" />
                    <testEntityAttribute testEntityAttributeName="lastname" testEntityAttributeType="NUMBER" testKotlinFieldType="kotlin.Int"/>
                    <testEntityAttribute testEntityAttributeName="nickname" testEntityAttributeType="BOOLEAN" testKotlinFieldType="kotlin.Boolean"/>
                </testEntity>
                <testEntity testEntityName="Address">
                    <testEntityAttribute testEntityAttributeName="street" testEntityAttributeType="TEXT"/>
                    <testEntityAttribute testEntityAttributeName="zip" testEntityAttributeType="TEXT"/>
                </testEntity>
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

        assertEquals(7, conceptDataList.size)

        val personRootNode = conceptDataList[0]
        assertEquals(testEntityConceptName, personRootNode.conceptName)
        assertEquals("Person", personRootNode.getFacet(testEntityNameFacetName))
        assertEquals("Person", personRootNode.getFacet(testEntityKotlinModelClassnameFacetName))
        assertEquals("org.codeblessing.sourceamazing.entities", personRootNode.getFacet(testEntityKotlinModelPackageFacetName))
        val firstnameNode = conceptDataList[1]
        assertEquals(testEntityAttributeConceptName, firstnameNode.conceptName)
        assertEquals("firstname", firstnameNode.getFacet(testEntityAttributeNameFacetName))
        assertEquals(AttributeTypeEnum.TEXT, firstnameNode.getFacet(testEntityAttributeTypeFacetName))
        assertEquals("kotlin.String", firstnameNode.getFacet(testKotlinFieldTypeFacetName))
        val addressRootNode = conceptDataList[4]
        assertEquals(testEntityConceptName, addressRootNode.conceptName)
        assertEquals("Address", addressRootNode.getFacet(testEntityNameFacetName))
        assertEquals("org.codeblessing.sourceamazing.entities", addressRootNode.getFacet(testEntityKotlinModelPackageFacetName))
    }


    @Schema
    private interface SaxParserTestSchema {
        @ChildConcepts(TestEntityConcept::class)
        fun getTestEntityChildren(): List<TestEntityConcept>
    }

    @Concept(testEntityConceptNameConst)
    private interface TestEntityConcept {
        @ChildConcepts(TestEntityAttributeConcept::class)
        fun getTestEntityAttributeChildren(): List<TestEntityAttributeConcept>

        @Facet(testEntityNameFacetNameConst)
        fun getName(): String
        @Facet(testEntityKotlinModelClassnameFacetNameConst)
        fun getKotlinModelClassname(): String
        @Facet(testEntityKotlinModelPackageFacetNameConst)
        fun getKotlinModelPackage(): String

    }
    @Concept(testEntityAttributeConceptNameConst)
    private interface TestEntityAttributeConcept {

        @Facet(testEntityAttributeNameFacetNameConst)
        fun getName(): String
        @Facet(testEntityAttributeTypeFacetNameConst)
        fun getType(): AttributeTypeEnum
        @Facet(testKotlinFieldTypeFacetNameConst)
        fun getKotlinType(): String


    }

    enum class AttributeTypeEnum {
        TEXT,
        NUMBER,
        BOOLEAN,
    }
    private fun createSchema(): SchemaAccess {
        return SchemaCreator.createSchemaFromSchemaDefinitionClass(SaxParserTestSchema::class.java)
    }
}

