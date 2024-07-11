package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongTypeMalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.EnumFacetAnnotationMirror
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaCreatorFacetEnumTypeAnnotationTest {

    @Test
    fun `test concept having an empty enumeration facet should not throw an exception`() {
        val emptyEnumerationClassMirror = CommonMirrors.enumClassMirror()
        val schemaMirror = SchemaMirrorDsl.schema {
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
        // TODO This check is currently not possible
        //  assertEquals(SchemaWithConceptWithEmptyEnumFacet.EmptyEnumeration::class, enumFacetSchema.enumerationType)
        assertEquals(0, enumFacetSchema.enumerationValues().size)
    }

    @Test
    fun `test concept having a enumeration facet`() {
        val seasonEnumerationClassMirror = CommonMirrors.enumClassMirror("WINTER", "SPRING", "SUMMER", "FALL")
        val schemaMirror = SchemaMirrorDsl.schema {
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
        assertEquals("MyEnumFacet", enumFacetSchema.facetName)
        assertEquals(FacetType.TEXT_ENUMERATION, enumFacetSchema.facetType)
        // TODO This check is currently not possible
        //  assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration::class, enumFacetSchema.enumerationType)
        assertEquals(4, enumFacetSchema.enumerationValues().size)

        // TODO This check is currently not possible
        //  assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.WINTER, enumFacetSchema.enumerationValues()[0])
        //  assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.SPRING, enumFacetSchema.enumerationValues()[1])
        //  assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.SUMMER, enumFacetSchema.enumerationValues()[2])
        //  assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.FALL, enumFacetSchema.enumerationValues()[3])
        assertEquals("WINTER", enumFacetSchema.enumerationValues()[0])
        assertEquals("SPRING", enumFacetSchema.enumerationValues()[1])
        assertEquals("SUMMER", enumFacetSchema.enumerationValues()[2])
        assertEquals("FALL", enumFacetSchema.enumerationValues()[3])
    }

    @Test
    fun `test invalid enum type on facet should throw an exception`() {
        val schemaMirror = SchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(EnumFacetAnnotationMirror(enumerationClass = CommonMirrors.stringClassMirror()))
                }
            }
        }

        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Schema(concepts = [SchemaWithConceptWithMissingEnumTypeOnFacet.ConceptClassWithEnumFacet::class])
    private interface SchemaWithConceptWithMissingEnumTypeOnFacet {
        @Concept(facets = [
            ConceptClassWithEnumFacet.MissingEnumTypeFacet::class,
        ])
        interface ConceptClassWithEnumFacet {
            @EnumFacet(enumerationClass = Unit::class)
            interface MissingEnumTypeFacet
        }
    }

    @Test
    fun `test enum facet with missing enum type should throw an exception`() {
        val schemaMirror = SchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(EnumFacetAnnotationMirror(enumerationClass = CommonMirrors.unitClassMirror()))
                }
            }
        }

        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }
}