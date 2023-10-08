package org.codeblessing.sourceamazing.engine.proxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

object ProxyCreator {

    fun <X:Any> createProxy(definitionClass: Class<X>, invocationHandler: InvocationHandler): X {
        @Suppress("UNCHECKED_CAST")
        return Proxy.newProxyInstance(
            this::class.java.classLoader, arrayOf<Class<*>>(definitionClass),
            invocationHandler
        ) as X // must be of type X as we declare it in the list of classes
    }

}
