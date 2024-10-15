package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedStringFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodParameterSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class BuilderMethodApiMethodAnnotationsTest {

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
        assertThrows(BuilderMethodParameterSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodParamWithSetConceptIdentifierAndIgnoreNullFacetValue::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithFixedFacetValueAndParameterFacetValue {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        @SetFixedStringFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.TextFacet::class, value = "fixed value")
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.TextFacet::class) myText: String,
        )
    }

    @Test
    fun `test string facet as fixed value and as parameter should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithFixedFacetValueAndParameterFacetValue::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithMultipleFixedFacetValues {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        @SetFixedStringFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.TextFacet::class, value = "fixed value 1")
        @SetFixedStringFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.TextFacet::class, value = "fixed value 2")
        fun doSomething()
    }

    @Test
    fun `test string facet with multiple fixed values should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithMultipleFixedFacetValues::class) { builder ->
                // do nothing
            }
        }
    }

}