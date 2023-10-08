package org.codeblessing.sourceamazing.xmlschema.schemacreator

import org.codeblessing.sourceamazing.api.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
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
        val xmlSchemaFileContent = XmlDomSchemaCreator.createSchemagicSchemaContent(schema)
        val xmlSchemaFileName = "sourceamazing-xml-schema.xsd"
        fileSystemAccess.writeFile(schemaDirectory.resolve(xmlSchemaFileName), xmlSchemaFileContent)
    }
}
