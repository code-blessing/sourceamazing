package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.DuplicateFacetMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MissingAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.NotInterfaceMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.IntFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.StringFacetAnnotationMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SchemaCreatorFacetAnnotationTest {

    @Test
    fun `test create a concept with an unannotated facet should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    // no facet annotation
                }
            }
        }
        assertThrows(MissingAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    fun `test create a concept with an non-interface facet should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(StringFacet())
                    setFacetIsNotInterface()
                }
            }
        }

        assertThrows(NotInterfaceMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    fun `test create facet class with a schema annotation should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(StringFacet())
                    withAnnotationOnFacet(Schema(emptyArray()))
                }
            }
        }

        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    fun `test create facet with multiple facet annotations should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(StringFacet())
                    withAnnotationOnFacet(IntFacet())
                }
            }
        }

        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    fun `test create facet class with a concept annotation should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(StringFacet())
                    withAnnotationOnFacet(Concept(emptyArray()))
                }
            }
        }

        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    fun `test duplicate facet class within a concept should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept(addConceptAnnotationWithAllFacets = false) {
                val facetClassMirror = facet {
                    withAnnotationOnFacet(StringFacet())
                }
                withAnnotationOnConcept(Concept(arrayOf(facetClassMirror, facetClassMirror)))
            }
        }
        assertThrows(DuplicateFacetMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    fun `test create an schema with concept class having a text facet`() {
        val conceptClassName = "MyConceptClassWithTextFacet"
        val facetClassName = "MyTextFacet"
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                withConceptClassName(conceptClassName)
                facet {
                    withFacetClassName(facetClassName)
                    withAnnotationOnFacet(StringFacet())
                }
            }
        }

        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        assertEquals(1, schema.numberOfConcepts())
        val concept = schema.allConcepts().first()
        assertEquals(conceptClassName, concept.conceptName)
        assertEquals(1, concept.facetNames.size)
        val facet = concept.facets.first()
        assertEquals(FacetType.TEXT, facet.facetType)
        assertEquals(facetClassName, facet.facetName)
    }

}