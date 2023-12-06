package org.codeblessing.sourceamazing.api.process.schema

import kotlin.reflect.KClass

interface DomainUnitSchemaHelper {
    fun <S: Any> createDomainUnitSchema(schemaDefinitionClass: KClass<S>): SchemaAccess
}
