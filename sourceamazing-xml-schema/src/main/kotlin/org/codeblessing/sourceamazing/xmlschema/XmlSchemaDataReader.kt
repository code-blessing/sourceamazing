package org.codeblessing.sourceamazing.xmlschema

import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.schema.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.schema.logger.LoggerFacade
import org.codeblessing.sourceamazing.xmlschema.parser.SaxParserHandler
import org.codeblessing.sourceamazing.xmlschema.xsdcreator.XmlSchemaInitializer
import java.nio.file.Path
import javax.xml.XMLConstants
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

object XmlSchemaDataReader {

    private const val SCHEMA_LANGUAGE = XMLConstants.W3C_XML_SCHEMA_NS_URI
    private const val SCHEMA_FEATURE = "http://apache.org/xml/features/validation/schema"

    fun createXsdSchemaAndReadXmlFile(
        xmlFile: Path,
        loggerFacade: LoggerFacade,
        placeholders: Map<String, String>,
        fileSystemAccess: FileSystemAccess,
        schemaAccess: SchemaAccess,
        dataCollector: ConceptDataCollector
    ) {
        val xmlParentDirectory = FileDirUtil.getDirectoryFromFile(xmlFile)
        val sourceAmazingSchemaXsd = XmlSchemaInitializer.initializeXmlSchemaFile(xmlFile, schemaAccess, fileSystemAccess)

        val factory: SAXParserFactory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = true
        factory.isValidating = false
        factory.setFeature(SCHEMA_FEATURE, true)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false)

        val sources = listOf(
            StreamSource(fileSystemAccess.fileAsInputStream(sourceAmazingSchemaXsd))
        )

        val schemaFactory: SchemaFactory = SchemaFactory.newInstance(SCHEMA_LANGUAGE)
        factory.schema = schemaFactory.newSchema(sources.toTypedArray())

        val saxParser: SAXParser = factory.newSAXParser()

        val saxParserHandler = SaxParserHandler(schemaAccess, dataCollector, placeholders, xmlParentDirectory, fileSystemAccess, loggerFacade)

        fileSystemAccess.fileAsInputStream(xmlFile).use {
            saxParser.parse(it, saxParserHandler)
        }
    }
}