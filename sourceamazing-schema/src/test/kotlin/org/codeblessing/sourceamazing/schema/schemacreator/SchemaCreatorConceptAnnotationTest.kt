package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaCreatorConceptAnnotationTest {

    @Schema(concepts = [SchemaWithEmptyConceptClass.EmptyConceptClass::class])
    private interface SchemaWithEmptyConceptClass {

        @Concept(facets = [])
        interface EmptyConceptClass
    }

    @Test
    fun `test create an schema with an empty concept class`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithEmptyConceptClass::class)
        assertEquals(1, schema.numberOfConcepts())
    }

}