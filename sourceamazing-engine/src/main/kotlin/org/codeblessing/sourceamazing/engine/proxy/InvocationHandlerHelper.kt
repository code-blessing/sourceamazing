package org.codeblessing.sourceamazing.engine.proxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

object InvocationHandlerHelper {

    fun handleObjectMethodsOrThrow(invocationHandler: InvocationHandler, method: Method?): Any {
        if(method != null) {
            if(method.name == "toString") {
                return invocationHandler.toString()
            }
            if(method.name == "hashCode") {
                return invocationHandler.hashCode()
            }
        }
        throw IllegalStateException("Method $method not annotated.")
    }

    fun throwIfProxyIsNull(proxy: Any?) {
        if(proxy == null) {
            throw IllegalStateException("No proxy defined.")
        }
    }

    fun requiredProxy(proxy: Any?, method: Method?): Any {
        if(proxy != null) {
            return proxy
        }
        throw IllegalStateException("Method $method has no proxy defined.")
    }

}
