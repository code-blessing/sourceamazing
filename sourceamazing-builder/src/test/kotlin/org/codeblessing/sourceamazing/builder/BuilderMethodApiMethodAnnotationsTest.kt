package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedBooleanFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedEnumFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedIntFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedStringFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
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
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithFixedFacetValueAndParameterFacetValue::class) { 
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
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithMultipleFixedFacetValues::class) { 
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
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithCorrectFixedEnumFacetValues::class) { 
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
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.WRONG_FACET_ENUM_VALUE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongFixedEnumFacetValues::class) { 
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
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.WRONG_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongFixedFacetType::class) { 
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
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_FACET) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUnregisteredFixedFacetType::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithValidFixedReference {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @NewConcept(concept = SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "bar")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "bar")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.SelfRefFacet::class, referencedConceptAlias = "bar")
        fun doSomething()
    }

    @Test
    fun `test referencing a concept with fixed alias should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithValidFixedReference::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithValidParameterReference {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @NewConcept(concept = SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "bar")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "bar")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.SelfRefFacet::class) myReference: ConceptIdentifier
        )
    }

    @Test
    fun `test referencing a concept with parameter value should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithValidParameterReference::class) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithFunctionInsteadOfConceptIdentifierReference {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @NewConcept(concept = SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "bar")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "bar")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.SelfRefFacet::class) myReference: () -> ConceptIdentifier
        )
    }

    @Test
    fun `test reference facet with function instead of ConceptIdentifier should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_SET_FACET_VALUE_PARAMETER) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithFunctionInsteadOfConceptIdentifierReference::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithStringInsteadOfConceptIdentifierReference {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @NewConcept(concept = SchemaWithConceptWithFacets.ConceptWithFacets::class, declareConceptAlias = "bar")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "bar")
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacets.ConceptWithFacets.SelfRefFacet::class) myReference: String
        )
    }

    @Test
    fun `test reference facet with string instead of ConceptIdentifier should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.BUILDER_PARAM_WRONG_REFERENCE_FACET_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacets::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithStringInsteadOfConceptIdentifierReference::class) {
                    // do nothing
                }
            }
        }
    }

}