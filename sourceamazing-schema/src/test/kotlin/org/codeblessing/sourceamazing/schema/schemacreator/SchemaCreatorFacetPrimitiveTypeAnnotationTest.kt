package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.typemirror.BooleanFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.IntFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.StringFacetAnnotationMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaCreatorFacetPrimitiveTypeAnnotationTest {

    @Test
    fun `test concept having three primitive type facet`() {
        val schemaMirror = SchemaMirrorDsl.schema {
            concept {
                facet {
                    facetMirror.withClassName("TextFacetClass")
                    facetMirror.withAnnotation(StringFacetAnnotationMirror())
                }
                facet {
                    facetMirror.withClassName("BooleanFacetClass")
                    facetMirror.withAnnotation(BooleanFacetAnnotationMirror())
                }
                facet {
                    facetMirror.withClassName("NumberFacetClass")
                    facetMirror.withAnnotation(IntFacetAnnotationMirror())
                }
            }
        }

        val schema = SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        assertEquals(1, schema.numberOfConcepts())
        val conceptSchema = schema.allConcepts().first()
        assertEquals(3, conceptSchema.facets.size)

        assertEquals("TextFacetClass", conceptSchema.facets[0].facetName.simpleName())
        assertEquals("BooleanFacetClass", conceptSchema.facets[1].facetName.simpleName())
        assertEquals("NumberFacetClass", conceptSchema.facets[2].facetName.simpleName())

        assertEquals(FacetType.TEXT, conceptSchema.facets[0].facetType)
        assertEquals(FacetType.BOOLEAN, conceptSchema.facets[1].facetType)
        assertEquals(FacetType.NUMBER, conceptSchema.facets[2].facetType)

        assertEquals(Unit::class, conceptSchema.facets[0].enumerationType)
        assertEquals(Unit::class, conceptSchema.facets[1].enumerationType)
        assertEquals(Unit::class, conceptSchema.facets[2].enumerationType)

        assertEquals(0, conceptSchema.facets[0].enumerationValues.size)
        assertEquals(0, conceptSchema.facets[1].enumerationValues.size)
        assertEquals(0, conceptSchema.facets[2].enumerationValues.size)

    }
}