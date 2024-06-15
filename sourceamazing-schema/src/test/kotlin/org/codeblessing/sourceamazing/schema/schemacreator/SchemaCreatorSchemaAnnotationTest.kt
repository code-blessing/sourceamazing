package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MissingAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.NotInterfaceMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.StringFacetAnnotationMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class SchemaCreatorSchemaAnnotationTest {

    @Test
    fun `test unannotated schema class should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema(addSchemaAnnotationWithAllConcepts = false) {
            // nothing to do
        }


        assertThrows(MissingAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test schema interface as class should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            setSchemaIsClass()
        }

        assertThrows(NotInterfaceMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test schema interface as enum class should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            setSchemaIsEnum()
        }

        assertThrows(NotInterfaceMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test schema interface as annotation interface should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            setSchemaIsAnnotation()
        }

        assertThrows(NotInterfaceMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test schema interface as object class interface should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            setSchemaIsObjectClass()
        }

        assertThrows(NotInterfaceMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test schema class with concept annotation should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            withAnnotationOnSchema(ConceptAnnotationMirror(emptyList()))
        }

        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test schema class with facet annotation should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            withAnnotationOnSchema(StringFacetAnnotationMirror())
        }

        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    @Disabled("Not prevented currently")
    fun `test schema with two schema annotations in hierarchy should throw an exception`() {
        val parentSchemaMirror = FakeSchemaMirrorDsl.schema {
            // parent schema
        }
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            withSuperClassMirror(parentSchemaMirror)
            // child schema
        }

        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test create an empty schema from an empty schema interface without throwing an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            // empty schema without concepts
        }
        val schema = SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        assertEquals(0, schema.numberOfConcepts())
    }
}