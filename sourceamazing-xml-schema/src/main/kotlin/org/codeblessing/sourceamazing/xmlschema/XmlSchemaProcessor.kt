package org.codeblessing.sourceamazing.xmlschema

import org.codeblessing.sourceamazing.schema.SchemaContextAccessor.toRevealedSchemaContext
import org.codeblessing.sourceamazing.schema.RevealedSchemaContext
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.xmlschema.api.XmlSchemaProcessorApi
import java.nio.file.Path

class XmlSchemaProcessor(): XmlSchemaProcessorApi {

    override fun createXsdSchemaAndReadXmlFile(schemaContext: SchemaContext, xmlFile: Path, placeholders: Map<String, String>) {
        val revealedSchemaContext: RevealedSchemaContext = schemaContext.toRevealedSchemaContext()
        XmlSchemaDataReader.createXsdSchemaAndReadXmlFile(
            xmlFile = xmlFile,
            loggerFacade = revealedSchemaContext.loggerFacade,
            placeholders = placeholders,
            fileSystemAccess = revealedSchemaContext.fileSystemAccess,
            schemaAccess = revealedSchemaContext.schema,
            dataCollector = revealedSchemaContext.conceptDataCollector,
        )
    }
}