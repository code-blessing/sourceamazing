package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.workOnRootInstance
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
    fun `test insert nothing to a text facet expecting zero to many texts will return an empty list`() {
        val schemaInstance =
            SchemaApi.withSchema<MyManyTexts> { schemaContext ->
                schemaContext.workOnRootInstance<MyManyTexts> {
                    // do add nothing
                }
            }

        Assertions.assertEquals(listOf<String>(), schemaInstance.zeroToManyStrings)
    }

    @Test
    fun `test insert three texts to a text facet expecting zero to many texts will return an list of three elements`() {
        val schemaInstance =
            SchemaApi.withSchema<MyManyTexts> { schemaContext ->
                schemaContext.workOnRootInstance<MyManyTexts> { conceptData ->
                    conceptData.addFacetValue(MyManyTexts::zeroToManyStrings, "hallo1")
                    conceptData.addFacetValue(MyManyTexts::zeroToManyStrings, "hallo2")
                    conceptData.addFacetValue(MyManyTexts::zeroToManyStrings, "hallo3")
                }
            }

        Assertions.assertEquals(listOf("hallo1", "hallo2", "hallo3"), schemaInstance.zeroToManyStrings)
    }

    @Test
    fun `test insert one text to a text facet expecting exactly one value will return that text`() {
        val schemaInstance =
            SchemaApi.withSchema<MyOneNonNullableText> { schemaContext ->
                schemaContext.workOnRootInstance<MyOneNonNullableText> { conceptData ->
                    conceptData.addFacetValue(MyOneNonNullableText::oneText, "hallo1")
                }
            }

        Assertions.assertEquals("hallo1", schemaInstance.oneText)
    }

    @Test
    fun `test insert nothing to a nullable text facet will return a null value`() {
        val schemaInstance =
            SchemaApi.withSchema<MyNullableText> { schemaContext ->
                schemaContext.workOnRootInstance<MyNullableText> {
                    // do add nothing
                }
            }

        Assertions.assertEquals(null, schemaInstance.maybeOneText)
    }

    @Test
    fun `test insert one text to a nullable text facet will return that text`() {
        val schemaInstance =
            SchemaApi.withSchema<MyNullableText> { schemaContext ->
                schemaContext.workOnRootInstance<MyNullableText> { conceptData ->
                    conceptData.addFacetValue(MyNullableText::maybeOneText, "hallo1")
                }
            }

        Assertions.assertEquals("hallo1", schemaInstance.maybeOneText)
    }
}
