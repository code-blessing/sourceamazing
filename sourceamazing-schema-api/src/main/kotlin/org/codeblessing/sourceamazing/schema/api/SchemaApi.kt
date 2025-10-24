package org.codeblessing.sourceamazing.schema.api

import java.util.*
import kotlin.reflect.KClass

object SchemaApi {

    fun <S : Any> withSchema(rootClazz: KClass<S>, schemaUsage: (schemaContext: SchemaContext) -> Unit): S {
        val schemaProcessorApis: ServiceLoader<SchemaProcessorApi> = ServiceLoader.load(SchemaProcessorApi::class.java)

        val schemaProcessorApi =
            requireNotNull(schemaProcessorApis.firstOrNull()) {
                "Could not find an implementation of the interface '${SchemaProcessorApi::class}'."
            }
        return schemaProcessorApi.withSchema(rootClazz, schemaUsage)
    }

    inline fun <reified S : Any> withSchema(noinline schemaUsage: (schemaContext: SchemaContext) -> Unit): S {
        return withSchema(S::class, schemaUsage)
    }
}
