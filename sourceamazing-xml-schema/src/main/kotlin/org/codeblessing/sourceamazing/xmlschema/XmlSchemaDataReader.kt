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
import kotlin.io.path.name

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
        val xmlDefinitionDirectory: Path = xmlFile.parent
        val xmlDefinitionFilename = xmlFile.name
        val xmlDefinitionFile = xmlDefinitionDirectory.resolve(xmlDefinitionFilename)
        val schemaDirectory = XmlSchemaInitializer.createSchemaDirectory(xmlDefinitionDirectory, fileSystemAccess)

        XmlSchemaInitializer.initializeXmlSchemaFile(schemaDirectory, schemaAccess, fileSystemAccess)


        val factory: SAXParserFactory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = true
        factory.isValidating = false
        factory.setFeature(SCHEMA_FEATURE, true)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false)

        val sourceamazingSchemaXsd = xmlDefinitionFile.parent.resolve("schema").resolve("sourceamazing-xml-schema.xsd")
        val sources = listOf(
            StreamSource(fileSystemAccess.fileAsInputStream(sourceamazingSchemaXsd))
        )

        val schemaFactory: SchemaFactory = SchemaFactory.newInstance(SCHEMA_LANGUAGE)
        factory.schema = schemaFactory.newSchema(sources.toTypedArray())

        val saxParser: SAXParser = factory.newSAXParser()

        val saxParserHandler = SaxParserHandler(schemaAccess, dataCollector, placeholders, xmlDefinitionDirectory, fileSystemAccess, loggerFacade)

        fileSystemAccess.fileAsInputStream(xmlDefinitionFile).use {
            saxParser.parse(it, saxParserHandler)
        }
    }
}