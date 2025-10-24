package org.codeblessing.sourceamazing.builder.factories

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible
import org.codeblessing.sourceamazing.builder.api.BuilderContext
import org.codeblessing.sourceamazing.builder.api.BuilderFactory
import org.codeblessing.sourceamazing.builder.typesafeapi.TypeSafeBuilderContext
import org.codeblessing.sourceamazing.builder.typesafeapi.adapter.TypeSafeBuilderContextAdapter
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.typesafeapi.TypeSafeSchemaContext
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.adapter.TypeSafeSchemaContextAdapter

class BuilderFactoriesHolder(val builderFactories: Set<BuilderFactory<*, *>>) {

    fun hasImplementation(builderClass: KClass<*>): Boolean {
        return builderFactories.any { it.builderClass == builderClass }
    }

    fun getFinalClass(builderClass: KClass<*>): KClass<*> {
        return builderFactories
            .filter { it.builderClass == builderClass }
            .map { it.builderImplementationClass }
            .firstOrNull() ?: builderClass
    }

    fun <B : Any> createImplementation(
        builderClass: KClass<B>,
        builder: B,
        schemaContext: TypeSafeSchemaContext,
        builderContext: TypeSafeBuilderContext,
    ): B {
        val factory = getBuilderFactory(builderClass)
        if (factory == null) {
            return builder
        }

        if (factory.builderImplementationSupplier != null) {
            @Suppress("UNCHECKED_CAST")
            val supplier = factory.builderImplementationSupplier as ((B, SchemaContext, BuilderContext) -> B)
            val nonTypeSafeSchemaContext: SchemaContext = TypeSafeSchemaContextAdapter(schemaContext)
            val nonTypeSafeBuilderContext: BuilderContext = TypeSafeBuilderContextAdapter(builderContext)
            return supplier(builder, nonTypeSafeSchemaContext, nonTypeSafeBuilderContext)
        }

        val implementationClass = getFinalClass(builderClass)
        val constructor =
            implementationClass.constructors.first {
                BuilderFactoryConstructorUtil.isValidConstructor(it, builderClass)
            }
        constructor.isAccessible = true

        val args = createArguments(constructor, builderClass, builder, schemaContext, builderContext)

        @Suppress("UNCHECKED_CAST")
        return constructor.callBy(args) as B
    }

    private fun <B : Any> createArguments(
        constructor: KFunction<Any>,
        builderClass: KClass<B>,
        builder: B,
        schemaContext: TypeSafeSchemaContext,
        builderContext: TypeSafeBuilderContext,
    ): Map<KParameter, Any?> {
        val nonTypeSafeSchemaContext: SchemaContext = TypeSafeSchemaContextAdapter(schemaContext)
        val nonTypeSafeBuilderContext: BuilderContext = TypeSafeBuilderContextAdapter(builderContext)
        return constructor.parameters
            .filter { !it.isOptional }
            .associateWith { kParameter ->
                when (kParameter.type.classifier) {
                    builderClass -> builder
                    SchemaContext::class -> nonTypeSafeSchemaContext
                    BuilderContext::class -> nonTypeSafeBuilderContext
                    else ->
                        throw IllegalArgumentException("${kParameter.type.classifier} is not allowed for $builderClass")
                }
            }
    }

    fun getBuilderFactory(builderClass: KClass<*>): BuilderFactory<*, *>? {
        return builderFactories.firstOrNull { it.builderClass == builderClass }
    }
}
