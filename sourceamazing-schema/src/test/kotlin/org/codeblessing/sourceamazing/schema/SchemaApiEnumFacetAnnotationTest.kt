package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.exceptions.WrongTypeSyntaxException
import org.junit.jupiter.api.Test

class SchemaApiEnumFacetAnnotationTest {

    private enum class MyPrivateEnum {
        @Suppress("UNUSED") A,
        @Suppress("UNUSED") B,
        @Suppress("UNUSED") C,

    }

    @Schema(concepts = [SchemaWithConceptWithPrivateEnumFacet.ConceptWithEnumFacet::class])
    private interface SchemaWithConceptWithPrivateEnumFacet {
        @Concept(facets = [ConceptWithEnumFacet.EnumerationFacet::class])
        interface ConceptWithEnumFacet {
            @EnumFacet(MyPrivateEnum::class)
            interface EnumerationFacet
        }
    }

    @Test
    fun `test create an schema with concept class having a enum facet that has modifier private should throw an exception`() {
        assertExceptionWithErrorCode(WrongTypeSyntaxException::class, SchemaErrorCode.FACET_ENUM_HAS_PRIVATE_MODIFIER) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithPrivateEnumFacet::class) {
                // do nothing
            }
        }
    }

    enum class MyPublicEnum {
        @Suppress("UNUSED") A,
        @Suppress("UNUSED") B,
        @Suppress("UNUSED") C,
    }

    @Schema(concepts = [SchemaWithConceptWithPublicEnumFacet.ConceptWithEnumFacet::class])
    private interface SchemaWithConceptWithPublicEnumFacet {
        @Concept(facets = [ConceptWithEnumFacet.EnumerationFacet::class])
        interface ConceptWithEnumFacet {
            @EnumFacet(MyPublicEnum::class)
            interface EnumerationFacet
        }
    }

    @Test
    fun `test create a schema with concept class having a public enum facet should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithPublicEnumFacet::class) {
            // do nothing
        }
    }
}