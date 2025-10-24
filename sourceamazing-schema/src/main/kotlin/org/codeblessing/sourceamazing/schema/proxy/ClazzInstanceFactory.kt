package org.codeblessing.sourceamazing.schema.proxy

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor
import org.codeblessing.sourceamazing.schema.clazzgraph.ClazzInstance
import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.utils.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.utils.type.isRegularClass

object ClazzInstanceFactory {
    fun <X : Any> createInstanceOrProxy(clazz: KClass<X>, clazzInstance: ClazzInstance): X {
        return if (clazz.isRegularClass) {
            createInstance(clazz, clazzInstance)
        } else {
            createProxy(clazz, clazzInstance)
        }
    }

    private fun <X : Any> createInstance(clazz: KClass<X>, clazzInstance: ClazzInstance): X {
        val primaryConstructor = requireNotNull(clazz.primaryConstructor)
        val arguments = createPrimaryConstructorArguments(primaryConstructor, clazzInstance)
        return primaryConstructor.call(*arguments)
    }

    private fun createPrimaryConstructorArguments(
        primaryConstructor: KFunction<*>,
        clazzInstance: ClazzInstance,
    ): Array<Any?> {
        return primaryConstructor.parameters
            .map { parameter ->
                val parameterName = requireNotNull(parameter.name) { "No name for parameter '$parameter'." }
                val classProperty = ClassProperty.of(parameterName)
                ClazzPropertyTypeAdapter.adaptClazzPropertyValue(classProperty, clazzInstance, parameter.type)
            }
            .toTypedArray()
    }

    private fun <X : Any> createProxy(clazz: KClass<X>, clazzInstance: ClazzInstance): X {
        return ProxyCreator.createProxy(
            interfaceForProxy = clazz,
            invocationHandler = ClazzInstanceInvocationHandler(clazzInstance),
        )
    }
}
