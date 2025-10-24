package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SchemaTraverserTest {

    private enum class MyEnum {
        PI, PA, PU
    }

    private interface MySchema {
        @Facet
        val name: String

        @Facet
        val nickNames: List<String>

        @Facet
        val noName: String?

        @Facet
        val really: Boolean

        @Facet
        val age: String?

        @Facet
        val piiPaaPuus: Set<MyEnum>

        @Facet
        @References([MySubConcept::class])
        val someSubElements: Set<MySubConcept>

    }

    private interface MySubConcept {
        @Facet
        val name: String
    }

    @Test
    fun createSchemaFromRootDefinitionClass() {

        val schema = SchemaTraverser.createSchemaFromRootDefinitionClass(MySchema::class)
        val rootConcept = schema.conceptByConceptName(ConceptName.of(MySchema::class))

        assertEquals(FacetType.TEXT, rootConcept.facets[0].facetType)

    }

}
