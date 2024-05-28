package org.codeblessing.sourceamazing.xmlschema.api

import org.codeblessing.sourceamazing.schema.api.SchemaContext
import java.nio.file.Path

interface XmlSchemaProcessorApi {

    fun createXsdSchemaAndReadXmlFile(schemaContext: SchemaContext, xmlFile: Path, placeholders: Map<String, String> = emptyMap())
}