package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test

class BuilderMethodApiMethodAnnotationsTest {

    private interface SchemaWithConceptWithFacets {
        enum class MyEnum {
            @Suppress("UNUSED") A,
            @Suppress("UNUSED") B,

        }

        interface ConceptWithFacets {
            @Suppress("UNUSED")
            @Facet
            val textFacet: String
            @Suppress("UNUSED")
            @Facet
            val boolFacet: Boolean
            @Suppress("UNUSED")
            @Facet
            val numberFacet: Int
            @Suppress("UNUSED")
            @Facet
            val enumerationFacet: MyEnum
            @Suppress("UNUSED")
            @Facet
            val selfRefFacet: ConceptWithFacets
        }

        @Suppress("UNUSED")
        @Facet
        val concept: ConceptWithFacets
    }


    @Builder
    private interface BuilderMethodWithFixedFacetValueAndParameterFacetValue {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacets.ConceptWithFacets::class)
        @SetRandomConceptIdentifierValue
        @SetFixedStringFacetValue(facetToModify = "textFacet", value = "fixed value")
        fun doSomething(
            @SetFacetValue(facetToModify = "textFacet") myText: String,
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
        @SetFixedStringFacetValue(facetToModify = "textFacet", value = "fixed value 1")
        @SetFixedStringFacetValue(facetToModify = "textFacet", value = "fixed value 2")
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
        @SetFixedEnumFacetValue(facetToModify = "enumerationFacet", value = "A")
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
        @SetFixedEnumFacetValue(facetToModify = "enumerationFacet", value = "NOT_A_NOR_B")
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
        @SetFixedBooleanFacetValue(facetToModify = "numberFacet", value = true)
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
        @SetFixedIntFacetValue(facetToModify = "unregisteredFacet", value = 42)
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
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet", referencedConceptAlias = "bar")
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
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet") myReference: ConceptIdentifier
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
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet") myReference: () -> ConceptIdentifier
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
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "selfRefFacet") myReference: String
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
