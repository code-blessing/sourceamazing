package org.codeblessing.sourceamazing.builder

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.allSupertypes

class ReflectionPlaygroundTest {

    interface ReflectionPlaygroundInterface<T>

    class ReflectionPlaygroundInterfaceInstance : ReflectionPlaygroundInterface<String>

    @Test
    fun `test reflection of kotlin`() {

        val interfaceClazz = ReflectionPlaygroundInterface::class

        val interfaceInstanceClazz = ReflectionPlaygroundInterfaceInstance::class

        Assertions.assertEquals("ReflectionPlaygroundInterface", interfaceClazz.simpleName)
        Assertions.assertEquals("ReflectionPlaygroundInterfaceInstance", interfaceInstanceClazz.simpleName)
    }
}