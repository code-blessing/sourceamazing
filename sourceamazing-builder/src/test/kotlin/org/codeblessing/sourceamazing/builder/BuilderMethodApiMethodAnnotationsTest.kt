package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedBooleanFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedEnumFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedIntFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedStringFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class BuilderMethodApiMethodAnnotationsTest {

    @Schema(concepts = [SchemaWithConceptWithFacets.ConceptWithFacets::class])
    private interface SchemaWithConceptWithFacets {
        enum class MyEnum {
            @Suppress("UNUSED") A,
            @Suppress("UNUSED") B,

        }

        @Concept(facets = [
            ConceptWithFacets.TextFacet::class,
            ConceptWithFacets.BoolFacet::class,
            ConceptWithFacets.NumberFacet::class,
            ConceptWithFacets.EnumerationFacet::class,
            ConceptWithFacets.SelfRefFacet::class,
        ])
        interface ConceptWithFacets {
            @StringFacet
            interface TextFacet
            @BooleanFacet
            interface BoolFacet
            @IntFacet
            interface NumberFacet
            @EnumFacet(MyEnum::class)
            interface EnumerationFacet
            @ReferenceFacet([ConceptWithFacets::class])
            interface SelfRefFacet
            @IntFacet
            interface UnregisteredFacet
        }
    }


    @Builder
    private interface BuilderMethodWithFixedFacetValueAndParameterFacetValue {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        @SetFixedStringFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class, value = "fixed value")
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class) myText: String,
        )
    }

    @Test
    fun `test string facet as fixed value and as parameter should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithFixedFacetValueAndParameterFacetValue::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithMultipleFixedFacetValues {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        @SetFixedStringFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class, value = "fixed value 1")
        @SetFixedStringFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.TextFacet::class, value = "fixed value 2")
        fun doSomething()
    }

    @Test
    fun `test string facet with multiple fixed values should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithMultipleFixedFacetValues::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCorrectFixedEnumFacetValues {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        @SetFixedEnumFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.EnumerationFacet::class, value = "A")
        fun doSomething()
    }

    @Test
    fun `test enum facet with fixed values having correct enum should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithCorrectFixedEnumFacetValues::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongFixedEnumFacetValues {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        @SetFixedEnumFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.EnumerationFacet::class, value = "NOT_A_NOR_B")
        fun doSomething()
    }

    @Test
    fun `test enum facet with fixed values having unknown enum value should throw an exception`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongFixedEnumFacetValues::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongFixedFacetType {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        @SetFixedBooleanFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.NumberFacet::class, value = true)
        fun doSomething()
    }

    @Test
    fun `test int facet with fixed boolean value should throw an exception`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongFixedFacetType::class) { builder ->
                    // do nothing
                }
            }
        }
    }
    @Builder
    private interface BuilderMethodWithUnregisteredFixedFacetType {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        @SetFixedIntFacetValue(facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.UnregisteredFacet::class, value = 42)
        fun doSomething()
    }

    @Test
    fun `test int facet with fixed value for unregistered facet should throw an exception`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUnregisteredFixedFacetType::class) { builder ->
                    // do nothing
                }
            }
        }
    }


}