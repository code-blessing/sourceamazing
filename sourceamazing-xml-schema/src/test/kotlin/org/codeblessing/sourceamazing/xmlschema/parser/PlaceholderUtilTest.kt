package org.codeblessing.sourceamazing.xmlschema.parser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class PlaceholderUtilTest {
    private val placeholders = mapOf(
        "dataDir" to "/foo/data",
        "imageDir" to "/foo/image",
        "emptyPlaceholder" to "",
    )

    @Test
    fun `should do nothing if no placeholder is in passed value`() {
        assertReplacement(
            expectedResult = "",
            template = "",
            placeholders = placeholders
        )
    }

    @Test
    fun `should replace placeholders in passed value`() {
        assertReplacement(
            expectedResult = "Put your data in /foo/data directory.",
            template = "Put your data in @{dataDir} directory.",
            placeholders = placeholders
        )
    }

    @Test
    fun `should replace multiple placeholders in passed value`() {
        assertReplacement(
            expectedResult = "Put your data in /foo/data directory and your images not in /foo/data but in /foo/image.",
            template = "Put your data in @{dataDir} directory and your images not in @{dataDir} but in @{imageDir}.",
            placeholders = placeholders
        )
    }

    @Test
    fun `should replace multiple placeholders directly at begin and end of template string`() {
        assertReplacement(
            expectedResult = "/foo/data is not /foo/image and /foo/image is not /foo/data",
            template = "@{dataDir} is not @{imageDir} and @{imageDir} is not @{dataDir}",
            placeholders = placeholders
        )
    }

    @Test
    fun `should replace multiple placeholders directly together in template string`() {
        assertReplacement(
            expectedResult = "Concatenate /foo/image/foo/data/foo/image together",
            template = "Concatenate @{imageDir}@{dataDir}@{imageDir} together",
            placeholders = placeholders
        )
    }

    @Test
    fun `should work properly with empty placeholders`() {
        assertReplacement(
            expectedResult = "not /foo/image and /foo/image is existing",
            template = "@{emptyPlaceholder}not @{imageDir} and @{imageDir} is existing@{emptyPlaceholder}",
            placeholders = placeholders
        )
    }

    @Test
    fun `should throw exception if placeholders could not be replaced`() {
        assertThrows(IllegalArgumentException::class.java
        ) { -> PlaceholderUtil.replacePlaceholders("A @{inexistentPlaceholder}.", placeholders) }
    }

    @Test
    fun `should replace placeholders that itself have placeholders in it (without resolving them recursively)`() {
        val placeholdersWithPlaceholders = mapOf(
            "dataDir" to "/foo/data",
            "imageDir" to "/@{dataDir}/image",
        )


        assertReplacement(
            expectedResult = "Put your data in /@{dataDir}/image directory.",
            template = "Put your data in @{imageDir} directory.",
            placeholders = placeholdersWithPlaceholders
        )
    }

    private fun assertReplacement(expectedResult: String, template: String, placeholders: Map<String, String>) {
        try {
            assertEquals(expectedResult,
                PlaceholderUtil.replacePlaceholders(template, placeholders)
            )

        } catch (assertionException: AssertionError) {
            throw assertionException
        } catch (e: Exception) {
            fail("Template: $template, Expected: $expectedResult, placeholders: $placeholders", e)
        }
    }

}
