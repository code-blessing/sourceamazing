package org.codeblessing.sourceamazing.xmlschema

import org.codeblessing.sourceamazing.schema.RevealedSchemaContext
import org.codeblessing.sourceamazing.schema.SchemaContextAccessor.toRevealedSchemaContext
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.xmlschema.api.XmlSchemaProcessorApi
import java.nio.file.Path

class XmlSchemaProcessor(): XmlSchemaProcessorApi {

    override fun createXsdSchemaAndReadXmlFile(schemaContext: SchemaContext, xmlFile: Path, placeholders: Map<String, String>) {
        val schemaContext: RevealedSchemaContext = schemaContext.toRevealedSchemaContext()
        XmlSchemaDataReader.createXsdSchemaAndReadXmlFile(
            xmlFile = xmlFile,
            loggerFacade = schemaContext.loggerFacade,
            placeholders = placeholders,
            fileSystemAccess = schemaContext.fileSystemAccess,
            schemaAccess = schemaContext.schema,
            dataCollector = schemaContext.dataCollector,
        )
    }
}
