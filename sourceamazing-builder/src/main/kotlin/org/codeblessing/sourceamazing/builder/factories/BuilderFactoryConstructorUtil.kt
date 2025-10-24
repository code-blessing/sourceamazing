package org.codeblessing.sourceamazing.builder.factories

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.valueParameters
import org.codeblessing.sourceamazing.builder.api.BuilderContext
import org.codeblessing.sourceamazing.schema.api.SchemaContext

object BuilderFactoryConstructorUtil {

    fun isValidConstructor(constructor: KFunction<*>, builderClass: KClass<*>): Boolean {
        if (constructor.extensionReceiverParameter != null) {
            return false
        }
        if (constructor.typeParameters.isNotEmpty()) {
            return false
        }
        if (
            constructor.valueParameters.any { valueParameter ->
                !isValidConstructorParameter(valueParameter, builderClass)
            }
        ) {
            return false
        }
        return true
    }

    private fun isValidConstructorParameter(valueParameter: KParameter, builderClass: KClass<*>): Boolean {
        if (valueParameter.isOptional) {
            return true
        }

        if (valueParameter.type.classifier == SchemaContext::class) {
            return true
        }

        if (valueParameter.type.classifier == BuilderContext::class) {
            return true
        }

        if (valueParameter.type.classifier == builderClass) {
            return true
        }

        return false
    }
}
