package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongCardinalityMalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.BooleanFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.IntFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.StringFacetAnnotationMirror
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaCreatorFacetCardinalityAnnotationTest {

    @Test
    fun `test concept having three facets with correct cardinalities`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror(minimumOccurrences =  0, maximumOccurrences = 1))
                }
                facet {
                    withAnnotationOnFacet(BooleanFacetAnnotationMirror(minimumOccurrences =  1, maximumOccurrences = 1))
                }
                facet {
                    withAnnotationOnFacet(IntFacetAnnotationMirror(minimumOccurrences =  2, maximumOccurrences = 5))
                }
            }
        }

        val schema = SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        val conceptSchema = schema.allConcepts().first()
        assertEquals(0, conceptSchema.facets[0].minimumOccurrences)
        assertEquals(1, conceptSchema.facets[0].maximumOccurrences)

        assertEquals(1, conceptSchema.facets[1].minimumOccurrences)
        assertEquals(1, conceptSchema.facets[1].maximumOccurrences)

        assertEquals(2, conceptSchema.facets[2].minimumOccurrences)
        assertEquals(5, conceptSchema.facets[2].maximumOccurrences)
    }


    @Test
    fun `test negative cardinality on facet should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror(minimumOccurrences =  -1, maximumOccurrences = 1))
                }
            }
        }

        Assertions.assertThrows(WrongCardinalityMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test min cardinality is greater than maximum cardinality on facet should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror(minimumOccurrences =  3, maximumOccurrences = 2))
                }
            }
        }

        Assertions.assertThrows(WrongCardinalityMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

}