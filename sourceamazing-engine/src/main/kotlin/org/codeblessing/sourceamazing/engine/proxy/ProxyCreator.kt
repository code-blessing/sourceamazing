package org.codeblessing.sourceamazing.engine.proxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

object ProxyCreator {

    fun <X:Any> createProxy(definitionClass: KClass<X>, invocationHandler: InvocationHandler): X {
        @Suppress("UNCHECKED_CAST")
        return Proxy.newProxyInstance(
            this::class.java.classLoader, arrayOf<Class<*>>(definitionClass.java),
            invocationHandler
        ) as X // must be of type X as we declare it in the list of classes
    }

}
