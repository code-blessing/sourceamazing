package org.codeblessing.sourceamazing.schema.api

import kotlin.reflect.KClass

interface SchemaProcessorApi {

    fun <S : Any> withSchema(schemaDefinitionClass: KClass<S>, schemaUsage: (schemaContext: SchemaContext)-> Unit): S
}