package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongTypeMalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.EnumFacetAnnotationMirror
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class SchemaCreatorFacetEnumTypeAnnotationTest {

    @Test
    fun `test concept having an empty enumeration facet should not throw an exception`() {
        val emptyEnumerationClassMirror = CommonFakeMirrors.namedEnumClassMirror("MyEnum")
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withFacetClassName("MyEnumFacet")
                    withAnnotationOnFacet(EnumFacetAnnotationMirror(enumerationClass = emptyEnumerationClassMirror))
                }
            }
        }

        val schema = SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)

        val conceptSchema = schema.allConcepts().first()
        val enumFacetSchema = conceptSchema.facets.first()
        assertEquals("MyEnumFacet", enumFacetSchema.facetName.simpleName())
        assertEquals(FacetType.TEXT_ENUMERATION, enumFacetSchema.facetType)
        assertNotNull(enumFacetSchema.enumerationType)
        assertEquals("MyEnum", requireNotNull(enumFacetSchema.enumerationType).className)
        assertEquals(0, enumFacetSchema.enumerationValues.size)
    }

    @Test
    fun `test concept having a enumeration facet`() {
        val seasonEnumerationClassMirror = CommonFakeMirrors.namedEnumClassMirror("MySeasonEnum", "WINTER", "SPRING", "SUMMER", "FALL")
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withFacetClassName("MyEnumFacet")
                    withAnnotationOnFacet(EnumFacetAnnotationMirror(enumerationClass = seasonEnumerationClassMirror))
                }
            }
        }

        val schema = SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)


        val conceptSchema = schema.allConcepts().first()
        val enumFacetSchema = conceptSchema.facets.first()
        assertEquals("MyEnumFacet", enumFacetSchema.facetName.simpleName())
        assertEquals(FacetType.TEXT_ENUMERATION, enumFacetSchema.facetType)
        assertEquals("MySeasonEnum", requireNotNull(enumFacetSchema.enumerationType).className)
        assertEquals(4, enumFacetSchema.enumerationValues.size)
        assertEquals("WINTER", enumFacetSchema.enumerationValues[0])
        assertEquals("SPRING", enumFacetSchema.enumerationValues[1])
        assertEquals("SUMMER", enumFacetSchema.enumerationValues[2])
        assertEquals("FALL", enumFacetSchema.enumerationValues[3])
    }

    @Test
    fun `test invalid enum type on facet should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(EnumFacetAnnotationMirror(enumerationClass = CommonFakeMirrors.stringClassMirror()))
                }
            }
        }

        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test enum facet with missing enum type should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(EnumFacetAnnotationMirror(enumerationClass = CommonFakeMirrors.unitClassMirror()))
                }
            }
        }

        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }
}