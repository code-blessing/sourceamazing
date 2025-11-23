package org.codeblessing.sourceamazing.xmlschema.xsdcreator

import org.codeblessing.sourceamazing.schema.api.SchemaAccess
import org.codeblessing.sourceamazing.utils.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.xmlschema.FileDirUtil
import java.nio.file.Path

object XmlSchemaInitializer {

    private const val XSD_SCHEMA_DIRECTORY = "schema"
    private const val XSD_SCHEMA_FILE_NAME = "sourceamazing-xml-schema.xsd"

    private fun createSchemaDirectory(xmlFile: Path, fileSystemAccess: FileSystemAccess): Path {
        val xmlFileDirectory = FileDirUtil.getDirectoryFromFile(xmlFile)
        val schemaDirectory = xmlFileDirectory.resolve(XSD_SCHEMA_DIRECTORY)
        fileSystemAccess.createDirectory(schemaDirectory)
        return schemaDirectory
    }

    fun initializeXmlSchemaFile(
        xmlFile: Path,
        schema: SchemaAccess,
        fileSystemAccess: FileSystemAccess,
    ): Path {
        val schemaDirectory = createSchemaDirectory(xmlFile, fileSystemAccess)
        val xmlSchemaFileContent = XmlDomSchemaCreator.createXsdSchemaContent(schema)
        val xsdFilePath = schemaDirectory.resolve(XSD_SCHEMA_FILE_NAME)
        fileSystemAccess.writeFile(xsdFilePath, xmlSchemaFileContent)
        return xsdFilePath
    }
}
