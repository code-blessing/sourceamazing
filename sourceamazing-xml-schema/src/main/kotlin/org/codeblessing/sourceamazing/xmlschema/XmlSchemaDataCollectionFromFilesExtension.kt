package org.codeblessing.sourceamazing.xmlschema

import org.codeblessing.sourceamazing.api.extensions.ExtensionName
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.ExtensionDataCollector
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionFromFilesExtension
import org.codeblessing.sourceamazing.api.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.api.logger.LoggerFacade
import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.xmlschema.parser.SaxParserHandler
import org.codeblessing.sourceamazing.xmlschema.schemacreator.XmlSchemaInitializer
import java.nio.file.Path
import javax.xml.XMLConstants
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import kotlin.io.path.name

class XmlSchemaDataCollectionFromFilesExtension: DataCollectionFromFilesExtension {

    private lateinit var schemaAccess: SchemaAccess;
    private lateinit var extensionDataCollector: ExtensionDataCollector;
    private lateinit var fileSystemAccess: FileSystemAccess;
    private lateinit var loggerFacade: LoggerFacade;
    private lateinit var parameterAccess: ParameterAccess;

    companion object {
        private val extensionName = ExtensionName.of("XmlSchemaInputExtension")
        private const val schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI
        private const val schemaFeature = "http://apache.org/xml/features/validation/schema"
    }

    override fun getExtensionName(): ExtensionName {
        return extensionName
    }

    override fun initializeDataCollectionExtension(
        loggerFacade: LoggerFacade,
        parameterAccess: ParameterAccess,
        fileSystemAccess: FileSystemAccess,
        schemaAccess: SchemaAccess,
        extensionDataCollector: ExtensionDataCollector
    ) {
        this.loggerFacade = loggerFacade
        this.parameterAccess = parameterAccess
        this.fileSystemAccess = fileSystemAccess
        this.extensionDataCollector = extensionDataCollector
        this.schemaAccess = schemaAccess
    }

    override fun readFromFiles(files: Set<Path>) {
        files.forEach { readFromFile(it) }
    }

    private fun readFromFile(file: Path) {
        val xmlDefinitionDirectory: Path = file.parent
        val xmlDefinitionFilename = file.name
        val xmlDefinitionFile = xmlDefinitionDirectory.resolve(xmlDefinitionFilename)
        val schemaDirectory = XmlSchemaInitializer.createSchemaDirectory(xmlDefinitionDirectory, fileSystemAccess)

        XmlSchemaInitializer.initializeXmlSchemaFile(schemaDirectory, schemaAccess, fileSystemAccess)


        val placeholders: Map<String, String> = parameterAccess.getParameterMap()

        val factory: SAXParserFactory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = true
        factory.isValidating = false
        factory.setFeature(schemaFeature, true)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false)

        val sourceamazingSchemaXsd = xmlDefinitionFile.parent.resolve("schema").resolve("sourceamazing-xml-schema.xsd")
        val sources = listOf<StreamSource>(
            StreamSource(fileSystemAccess.fileAsInputStream(sourceamazingSchemaXsd))
        )

        val schemaFactory: SchemaFactory = SchemaFactory.newInstance(schemaLanguage)
        factory.schema = schemaFactory.newSchema(sources.toTypedArray())

        val saxParser: SAXParser = factory.newSAXParser()

        val saxParserHandler = SaxParserHandler(schemaAccess, extensionDataCollector, placeholders, xmlDefinitionDirectory, fileSystemAccess, loggerFacade)

        fileSystemAccess.fileAsInputStream(xmlDefinitionFile).use {
            saxParser.parse(it, saxParserHandler)
        }
    }
}
