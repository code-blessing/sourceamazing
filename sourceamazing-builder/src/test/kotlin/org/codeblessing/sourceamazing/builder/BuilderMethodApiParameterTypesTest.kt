package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class BuilderMethodApiParameterTypesTest {

    @Schema(concepts = [SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class])
    private interface SchemaWithConceptWithTextFacet {
        @Concept(facets = [ConceptWithTextFacet.TextFacet::class])
        interface ConceptWithTextFacet {
            @StringFacet
            interface TextFacet
        }
    }

    @Builder
    private interface BuilderMethodParamWithSetConceptIdentifierAndIgnoreNullFacetValue {
        interface MyConceptClass

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(MyConceptClass::class)
        fun doSomething(
            @IgnoreNullFacetValue @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
        )
    }

    @Test
    fun `test IgnoreNullFacetValue annotation and ConceptIdentifierValue annotation on same method should throw an exception`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodParamWithSetConceptIdentifierAndIgnoreNullFacetValue::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithIllegalConceptIdClass {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptId: String,
        )
    }

    @Test
    fun `test concept id parameter without ConceptIdentifier class should throw an exception`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithIllegalConceptIdClass::class) { builder ->
                    // do nothing
                }
            }
        }
    }

}