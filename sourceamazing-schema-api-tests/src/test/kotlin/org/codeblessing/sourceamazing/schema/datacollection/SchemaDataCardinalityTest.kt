package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SchemaDataCardinalityTest {

    private interface MyManyTexts {

        val zeroToManyStrings: List<String>
    }

    private interface MyOneNonNullableText {

        val oneText: String
    }

    private interface MyNullableText {

        val maybeOneText: String?
    }

    @Test
    fun `test insert nothing to a text clazzProperty expecting zero to many texts will return an empty list`() {
        val schemaInstance =
            SchemaApi.withSchema<MyManyTexts> {
                // nothing to do
            }

        Assertions.assertEquals(listOf<String>(), schemaInstance.zeroToManyStrings)
    }

    @Test
    fun `test insert three texts to a text clazzProperty expecting zero to many texts will return an list of three elements`() {
        val schemaInstance =
            SchemaApi.withSchema<MyManyTexts> { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().let { clazzData ->
                    clazzData.addClazzPropertyValue("zeroToManyStrings", "hallo1")
                    clazzData.addClazzPropertyValue("zeroToManyStrings", "hallo2")
                    clazzData.addClazzPropertyValue("zeroToManyStrings", "hallo3")
                }
            }

        Assertions.assertEquals(listOf("hallo1", "hallo2", "hallo3"), schemaInstance.zeroToManyStrings)
    }

    @Test
    fun `test insert one text to a text clazzProperty expecting exactly one value will return that text`() {
        val schemaInstance =
            SchemaApi.withSchema<MyOneNonNullableText> { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("oneText", "hallo1")
            }

        Assertions.assertEquals("hallo1", schemaInstance.oneText)
    }

    @Test
    fun `test insert nothing to a nullable text clazzProperty will return a null value`() {
        val schemaInstance =
            SchemaApi.withSchema<MyNullableText> {
                // nothing to do
            }

        Assertions.assertEquals(null, schemaInstance.maybeOneText)
    }

    @Test
    fun `test insert one text to a nullable text clazzProperty will return that text`() {
        val schemaInstance =
            SchemaApi.withSchema<MyNullableText> { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("maybeOneText", "hallo1")
            }

        Assertions.assertEquals("hallo1", schemaInstance.maybeOneText)
    }
}
