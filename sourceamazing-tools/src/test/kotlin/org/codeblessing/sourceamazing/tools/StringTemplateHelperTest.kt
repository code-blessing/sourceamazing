package org.codeblessing.sourceamazing.tools

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class StringTemplateHelperTest {

    @Test
    fun `onlyIf returns empty string in case of false condition`() {
        assertEquals("", StringTemplateHelper.onlyIf(false, "text for true"))
    }

    @Test
    fun `onlyIf returns given string in case of true condition`() {
        assertEquals("text for true", StringTemplateHelper.onlyIf(true, "text for true"))
    }

    @Test
    fun `ifElse returns else string in case of false condition`() {
        assertEquals("text else", StringTemplateHelper.ifElse(false, "text for true", "text else"))
    }

    @Test
    fun `ifElse returns given string in case of true condition`() {
        assertEquals("text for true", StringTemplateHelper.ifElse(true, "text for true", "text else"))
    }

    open class BaseType {
        override fun toString(): String {
            return "I am a base type"
        }
    }

    class SubType: BaseType() {
        override fun toString(): String {
            return "I am a sub type"
        }

    }

    @Test
    fun `onlyIfIsInstance return empty text if it is null`() {
        assertEquals("", StringTemplateHelper.onlyIfIsInstance<SubType>(null) {
            it.toString()
        })
    }

    @Test
    fun `onlyIfIsInstance return empty text if it is not a subtype`() {
        val baseTypeInstance= BaseType()
        assertEquals("", StringTemplateHelper.onlyIfIsInstance<SubType>(baseTypeInstance) {
            it.toString()
        })
    }

    @Test
    fun `onlyIfIsInstance return given text if it is a subtype`() {
        val subTypeInstance= SubType()
        assertEquals("I am a sub type", StringTemplateHelper.onlyIfIsInstance<SubType>(subTypeInstance) {
            it.toString()
        })
    }

}
