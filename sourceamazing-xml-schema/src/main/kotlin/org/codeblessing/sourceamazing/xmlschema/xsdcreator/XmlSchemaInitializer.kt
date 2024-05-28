package org.codeblessing.sourceamazing.xmlschema.xsdcreator

import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.filesystem.FileSystemAccess
import java.nio.file.Path

object XmlSchemaInitializer {

    fun createSchemaDirectory(definitionDirectory: Path, fileSystemAccess: FileSystemAccess): Path {
        val schemaDirectory = definitionDirectory.resolve("schema")
        fileSystemAccess.createDirectory(schemaDirectory)
        return schemaDirectory
    }

    fun initializeXmlSchemaFile(
        schemaDirectory: Path,
        schema: SchemaAccess,
        fileSystemAccess: FileSystemAccess
    ) {
        val xmlSchemaFileContent = XmlDomSchemaCreator.createXsdSchemaContent(schema)
        val xmlSchemaFileName = "sourceamazing-xml-schema.xsd"
        fileSystemAccess.writeFile(schemaDirectory.resolve(xmlSchemaFileName), xmlSchemaFileContent)
    }
}
