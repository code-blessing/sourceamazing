package org.codeblessing.sourceamazing.tools

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class StringIdentHelperTest {

    @Test
    fun `text without marker is left unchanged`() {
        val text = """
            This is the text.
            There are multiple lines.
        """.trimIndent()

        assertEquals(text, StringIdentHelper.insertIdentForMarker(text))

    }

    @Test
    fun `text marker is removed from text on the same line`() {
        val text = """
            This is the text.
            There are ${StringIdentHelper.marker}no multiple${StringIdentHelper.marker} lines.
        """.trimIndent()

        val expectedText = """
            This is the text.
            There are no multiple lines.
        """.trimIndent()

        assertEquals(expectedText, StringIdentHelper.insertIdentForMarker(text))

    }


    @Test
    fun `text marker is removed from text and ident is inserted per line`() {
        val multipleLines = listOf("multiple lines", "with no ident", "will have more ident").joinToString("\n")

        val text = """
            this is the text where...
            ${StringIdentHelper.marker}${multipleLines}${StringIdentHelper.marker}
            after adding ident.
        """.trimIndent()

        val expectedText = """
            this is the text where...
            multiple lines
            with no ident
            will have more ident
            after adding ident.
        """.trimIndent()

        assertEquals(expectedText, StringIdentHelper.insertIdentForMarker(text))

    }

}
