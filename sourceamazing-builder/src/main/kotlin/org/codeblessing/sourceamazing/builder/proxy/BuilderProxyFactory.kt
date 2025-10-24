package org.codeblessing.sourceamazing.builder.proxy

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.builder.factories.BuilderFactoriesHolder
import org.codeblessing.sourceamazing.builder.typesafeapi.TypeSafeBuilderContext
import org.codeblessing.sourceamazing.schema.typesafeapi.TypeSafeSchemaContext
import org.codeblessing.sourceamazing.schema.utils.proxy.ProxyCreator

object BuilderProxyFactory {

    fun <B : Any> createNewBuilder(
        schemaContext: TypeSafeSchemaContext,
        builderClass: KClass<B>,
        builderFactoriesHolder: BuilderFactoriesHolder,
        builderContext: TypeSafeBuilderContext,
    ): B {
        val builderProxy =
            createNewBuilderProxy(
                schemaContext = schemaContext,
                builderClass = builderClass,
                builderFactoriesHolder = builderFactoriesHolder,
                aliases = builderContext,
            )

        return if (builderFactoriesHolder.hasImplementation(builderClass)) {
            builderFactoriesHolder.createImplementation(builderClass, builderProxy, schemaContext, builderContext)
        } else {
            builderProxy
        }
    }

    private fun <B : Any> createNewBuilderProxy(
        schemaContext: TypeSafeSchemaContext,
        builderClass: KClass<B>,
        builderFactoriesHolder: BuilderFactoriesHolder,
        aliases: TypeSafeBuilderContext,
    ): B {
        return ProxyCreator.createProxy(
            interfaceForProxy = builderClass,
            invocationHandler =
                BuilderInvocationHandler(
                    schemaContext = schemaContext,
                    builderClass = builderClass,
                    builderFactoriesHolder = builderFactoriesHolder,
                    superiorAliases = aliases,
                ),
        )
    }
}
