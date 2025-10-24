package org.codeblessing.sourceamazing.builder.api

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.SchemaContext

infix fun <B : Any, I : B> KClass<B>.by(implementation: KClass<I>): BuilderFactory<B, I> {
    return BuilderFactory(builderClass = this, builderImplementationClass = implementation)
}

fun <B : Any, I : B> KClass<B>.create(
    implementation: KClass<I>,
    supplier: (B, SchemaContext, BuilderContext) -> I,
): BuilderFactory<B, I> {
    return BuilderFactory(
        builderClass = this,
        builderImplementationClass = implementation,
        builderImplementationSupplier = supplier,
    )
}
