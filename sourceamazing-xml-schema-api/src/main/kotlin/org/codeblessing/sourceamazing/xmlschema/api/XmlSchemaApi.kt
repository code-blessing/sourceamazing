package org.codeblessing.sourceamazing.xmlschema.api

import org.codeblessing.sourceamazing.schema.api.SchemaContext
import java.nio.file.Path
import java.util.*

object XmlSchemaApi {

    fun createXsdSchemaAndReadXmlFile(schemaContext: SchemaContext, xmlFile: Path, placeholders: Map<String, String> = emptyMap()) {
        val xmlSchemaProcessorApis: ServiceLoader<XmlSchemaProcessorApi> = ServiceLoader.load(XmlSchemaProcessorApi::class.java)

        val xmlSchemaProcessorApi = requireNotNull(xmlSchemaProcessorApis.firstOrNull()) {
            "Could not find an implementation of the interface '${XmlSchemaProcessorApi::class}'."
        }
        xmlSchemaProcessorApi.createXsdSchemaAndReadXmlFile(schemaContext, xmlFile, placeholders)

    }
}