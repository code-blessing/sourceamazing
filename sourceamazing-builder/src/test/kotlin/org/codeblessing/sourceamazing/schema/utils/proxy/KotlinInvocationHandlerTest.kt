package org.codeblessing.sourceamazing.schema.utils.proxy

import kotlin.reflect.KFunction
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class KotlinInvocationHandlerTest {

    class MyKotlinInvocationHandler() :
        KotlinInvocationHandler(allowMemberProperties = false, allowMemberFunctions = false) {
        override fun invoke(proxy: Any, function: KFunction<*>, arguments: List<Any?>): Any? {
            return "42"
        }
    }

    interface EmptyInterface

    @Test
    fun `test that the toString method can be called on a proxy without exceptions`() {
        val proxy =
            ProxyCreator.createProxy(
                interfaceForProxy = EmptyInterface::class,
                invocationHandler = MyKotlinInvocationHandler(),
            )

        proxy.toString()
    }

    @Test
    fun `test that the hashCode method can be called on a proxy without exceptions`() {
        val proxy =
            ProxyCreator.createProxy(
                interfaceForProxy = EmptyInterface::class,
                invocationHandler = MyKotlinInvocationHandler(),
            )

        proxy.hashCode()
    }

    @Test
    fun `test that the equals method can be called on a proxy without exceptions`() {
        val proxy =
            ProxyCreator.createProxy(
                interfaceForProxy = EmptyInterface::class,
                invocationHandler = MyKotlinInvocationHandler(),
            )

        proxy.equals("other")
    }

    @Test
    fun `test that the equals method compared to itself return false as comparing proxies does not make sense`() {
        val proxy =
            ProxyCreator.createProxy(
                interfaceForProxy = EmptyInterface::class,
                invocationHandler = MyKotlinInvocationHandler(),
            )

        assertFalse(proxy.equals(proxy))
    }

    @Test
    fun `test that the equals method compares to false for two different instances`() {
        val proxy1 =
            ProxyCreator.createProxy(
                interfaceForProxy = EmptyInterface::class,
                invocationHandler = MyKotlinInvocationHandler(),
            )

        val proxy2 =
            ProxyCreator.createProxy(
                interfaceForProxy = EmptyInterface::class,
                invocationHandler = MyKotlinInvocationHandler(),
            )

        assertFalse(proxy1.equals(proxy2))
    }
}
