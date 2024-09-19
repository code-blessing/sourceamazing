package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SchemaCreatorFacetPrimitiveTypeAnnotationTest {

    @Test
    fun `test concept having three primitive type facet`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withFacetClassName("TextFacetClass")
                    withAnnotationOnFacet(StringFacet())
                }
                facet {
                    withFacetClassName("BooleanFacetClass")
                    withAnnotationOnFacet(BooleanFacet())
                }
                facet {
                    withFacetClassName("NumberFacetClass")
                    withAnnotationOnFacet(IntFacet())
                }
            }
        }

        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        assertEquals(1, schema.numberOfConcepts())
        val conceptSchema = schema.allConcepts().first()
        assertEquals(3, conceptSchema.facets.size)

        assertEquals("TextFacetClass", conceptSchema.facets[0].facetName.simpleName())
        assertEquals("BooleanFacetClass", conceptSchema.facets[1].facetName.simpleName())
        assertEquals("NumberFacetClass", conceptSchema.facets[2].facetName.simpleName())

        assertEquals(FacetType.TEXT, conceptSchema.facets[0].facetType)
        assertEquals(FacetType.BOOLEAN, conceptSchema.facets[1].facetType)
        assertEquals(FacetType.NUMBER, conceptSchema.facets[2].facetType)

        assertNull(conceptSchema.facets[0].enumerationType)
        assertNull(conceptSchema.facets[1].enumerationType)
        assertNull(conceptSchema.facets[2].enumerationType)

        assertEquals(0, conceptSchema.facets[0].enumerationValues.size)
        assertEquals(0, conceptSchema.facets[1].enumerationValues.size)
        assertEquals(0, conceptSchema.facets[2].enumerationValues.size)

    }
}