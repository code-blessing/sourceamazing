package org.codeblessing.sourceamazing.schema.type

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KClassUtilTest {

    @Test
    fun `base class of an empty list return`() {
        assertTrue(KClassUtil.findAllCommonBaseClasses(emptyList()).isEmpty())
    }

    @Test
    fun `base classes of a single class contains the single class itself`() {
        val baseClasses = KClassUtil.findAllCommonBaseClasses(listOf(String::class))
        assertEquals(5, baseClasses.size)
        assertTrue(baseClasses.contains(String::class))
    }

    @Test
    fun `base class of multiple same classes contains the class itself`() {
        val baseClasses = KClassUtil.findAllCommonBaseClasses(listOf(String::class, String::class, String::class))
        assertEquals(5, baseClasses.size)
        assertTrue(baseClasses.contains(String::class))
    }

    @Test
    fun `base class of two different classes with common base class contains only its base class`() {
        val baseClasses = KClassUtil.findAllCommonBaseClasses(listOf(Double::class, Float::class))
        assertEquals(3, baseClasses.size)
        assertTrue(baseClasses.contains(Number::class))
        assertFalse(baseClasses.contains(Double::class))
        assertFalse(baseClasses.contains(Float::class))
    }

    @Test
    fun `two different classes without common base class contains only the Any class`() {
        val baseClasses = KClassUtil.findAllCommonBaseClasses(listOf(String::class, Float::class))
        assertEquals(3, baseClasses.size)
        assertTrue(baseClasses.contains(Any::class))
        assertFalse(baseClasses.contains(String::class))
        assertFalse(baseClasses.contains(Float::class))
    }

    @Test
    fun `base class of Any class is Any`() {
        val baseClasses = KClassUtil.findAllCommonBaseClasses(listOf(Any::class, Float::class))
        assertEquals(1, baseClasses.size)
        assertTrue(baseClasses.contains(Any::class))
        assertFalse(baseClasses.contains(Float::class))
    }
}