package org.codeblessing.sourceamazing.builder.api

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.SchemaContext

data class BuilderFactory<B : Any, I : B>(
    val builderClass: KClass<B>,
    val builderImplementationClass: KClass<I>,
    val builderImplementationSupplier: ((B, SchemaContext, BuilderContext) -> I)? = null,
)
