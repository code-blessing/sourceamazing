package org.codeblessing.sourceamazing.api.process.schema

import org.codeblessing.sourceamazing.api.process.schema.exceptions.MalformedSchemaException
import kotlin.jvm.Throws

interface DomainUnitSchemaHelper {
    @Throws(MalformedSchemaException::class)
    fun <S: Any> createDomainUnitSchema(schemaDefinitionClass: Class<S>): SchemaAccess
}
