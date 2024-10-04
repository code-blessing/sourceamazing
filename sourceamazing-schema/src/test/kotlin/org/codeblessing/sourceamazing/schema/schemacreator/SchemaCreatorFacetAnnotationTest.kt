package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SchemaCreatorFacetAnnotationTest {

    @Schema(concepts = [SchemaWithConceptWithTextFacetClass.ConceptClassWithTextFacet::class])
    private interface SchemaWithConceptWithTextFacetClass {
        @Concept(facets = [ConceptClassWithTextFacet.TextFacetClass::class])
        interface ConceptClassWithTextFacet {
            @StringFacet
            interface TextFacetClass
        }
    }

    @Test
    fun `test create an schema with concept class having a text facet`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithTextFacetClass::class)
        assertEquals(1, schema.numberOfConcepts())
        val concept = schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithTextFacetClass.ConceptClassWithTextFacet::class))
        assertTrue(concept.hasFacet(FacetName.of(SchemaWithConceptWithTextFacetClass.ConceptClassWithTextFacet.TextFacetClass::class)))
    }

}