package org.codeblessing.sourceamazing.schema.api

import java.util.*
import kotlin.reflect.KClass

object SchemaApi {

    fun <S : Any> withSchema(schemaDefinitionClass: KClass<S>, schemaUsage: (schemaContext: SchemaContext)-> Unit): S {
        val schemaProcessorApis: ServiceLoader<SchemaProcessorApi> = ServiceLoader.load(SchemaProcessorApi::class.java)

        val schemaProcessorApi = requireNotNull(schemaProcessorApis.firstOrNull()) {
            "Could not find an implementation of the interface '${SchemaProcessorApi::class}'."
        }
        return schemaProcessorApi.withSchema(schemaDefinitionClass, schemaUsage)

    }
}