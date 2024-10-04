package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.junit.jupiter.api.Test

class SchemaQueryValidatorTest {

    private interface CommonConceptInterface

    @Concept(facets = [])
    private interface OneConceptClass: CommonConceptInterface

    @Concept(facets = [])
    private interface OtherConceptClass: CommonConceptInterface

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithoutAccessorMethods

    @Test
    fun `test schema without accessor method should return without exception`() {
        SchemaQueryValidator.validateAccessorMethodsOfSchemaDefinitionClass(SchemaWithoutAccessorMethods::class)
    }
}