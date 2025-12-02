package org.codeblessing.sourceamazing.xmlschema

import java.nio.file.Path
import org.codeblessing.sourceamazing.schema.RevealedSchemaContext
import org.codeblessing.sourceamazing.schema.SchemaContextAccessor.toRevealedSchemaContext
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.xmlschema.api.XmlSchemaProcessorApi

class XmlSchemaProcessor() : XmlSchemaProcessorApi {

    override fun createXsdSchemaAndReadXmlFile(
        schemaContext: SchemaContext,
        xmlFile: Path,
        placeholders: Map<String, String>,
    ) {
        val schemaContextImpl: RevealedSchemaContext = schemaContext.toRevealedSchemaContext()
        XmlSchemaDataReader.createXsdSchemaAndReadXmlFile(
            xmlFile = xmlFile,
            loggerFacade = schemaContextImpl.loggerFacade,
            placeholders = placeholders,
            fileSystemAccess = schemaContextImpl.fileSystemAccess,
            schemaAccess = schemaContext.schema,
            dataCollector = schemaContext.dataCollector,
        )
    }
}
