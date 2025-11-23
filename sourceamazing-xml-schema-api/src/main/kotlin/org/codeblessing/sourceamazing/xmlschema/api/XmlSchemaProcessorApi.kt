package org.codeblessing.sourceamazing.xmlschema.api

import java.nio.file.Path
import org.codeblessing.sourceamazing.schema.api.SchemaContext

interface XmlSchemaProcessorApi {

    fun createXsdSchemaAndReadXmlFile(
        schemaContext: SchemaContext,
        xmlFile: Path,
        placeholders: Map<String, String> = emptyMap(),
    )
}
