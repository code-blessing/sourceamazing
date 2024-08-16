package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.DuplicateConceptMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MissingAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.NotInterfaceMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.StringFacetAnnotationMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class SchemaCreatorConceptAnnotationTest {

    @Test
    fun `test unannotated concept class should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept(addConceptAnnotationWithAllFacets = false) {
                // concept without concept annotation
            }
        }
        assertThrows(MissingAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test non-interface concept class should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                setConceptIsClass()
            }
        }

        assertThrows(NotInterfaceMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test create concept class with a schema annotation should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                withAnnotationOnConcept(SchemaAnnotationMirror(emptyList()))
            }
        }

        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test concept class with a facet annotation should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                withAnnotationOnConcept(StringFacetAnnotationMirror())
            }
        }
        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test duplicate concept classes should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema(addSchemaAnnotationWithAllConcepts = false) {
            val conceptClassMirror = concept {
                // a concept
            }
            withAnnotationOnSchema(SchemaAnnotationMirror(listOf(conceptClassMirror, conceptClassMirror)))
        }
        assertThrows(DuplicateConceptMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    @Disabled("Not prevented currently")
    fun `test concept with two concept annotations in hierarchy should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            val parentConcept = concept {
                // parentConcept
            }
            concept {
                // childConcept

                withSuperClassMirror(parentConcept)
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test create an schema with an empty concept class`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                // empty concept class
            }
        }

        val schema = SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        assertEquals(1, schema.numberOfConcepts())
    }

}