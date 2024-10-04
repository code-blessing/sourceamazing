package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaCreatorSchemaAnnotationTest {

    @Schema(concepts = [])
    private interface EmptySchemaDefinitionClass

    @Test
    fun `test create an empty schema from an empty schema interface`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(EmptySchemaDefinitionClass::class)
        assertEquals(0, schema.numberOfConcepts())
    }
}