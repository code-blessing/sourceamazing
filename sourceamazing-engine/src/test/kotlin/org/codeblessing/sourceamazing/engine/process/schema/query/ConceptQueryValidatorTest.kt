package org.codeblessing.sourceamazing.engine.process.schema.query

import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.api.process.schema.annotations.FacetType
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConceptId
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryFacet
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.MalformedSchemaException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ConceptQueryValidatorTest {

    @Schema(concepts = [ SchemaWithoutAccessorMethods.ConceptWithoutAccessorMethods::class])
    private interface SchemaWithoutAccessorMethods {
        @Concept(facets = [])
        interface ConceptWithoutAccessorMethods
    }

    @Test
    fun `test concept without accessor method should return without exception`() {
        ConceptQueryValidator.validateAccessorMethodsOfConceptClass(SchemaWithoutAccessorMethods.ConceptWithoutAccessorMethods::class)
    }

    @Schema(concepts = [ SchemaWithUnannotatedAccessorMethods.OneConcept::class ])
    private interface SchemaWithUnannotatedAccessorMethods {
        @Concept(facets = [OneConcept.OneFacet::class])
        interface OneConcept {

            @Facet(FacetType.TEXT)
            interface OneFacet

            fun getFacetValue(): List<Any>
        }
    }

    @Test
    fun `test concept with a unannotated method should throw an exception`() {
        assertThrows(MalformedSchemaException::class.java) {
            ConceptQueryValidator.validateAccessorMethodsOfConceptClass(SchemaWithUnannotatedAccessorMethods.OneConcept::class)
        }
    }


    @Schema(concepts = [ SchemaWithValidAnnotatedAccessorMethods.OneConcept::class ])
    private interface SchemaWithValidAnnotatedAccessorMethods {
        @Concept(facets = [OneConcept.OneFacet::class])
        interface OneConcept {

            @Facet(FacetType.TEXT)
            interface OneFacet

            @QueryFacet(OneFacet::class)
            fun getFacetValue(): List<Any>
        }
    }

    @Test
    fun `test concept with a valid annotated method should return without exception`() {
        ConceptQueryValidator.validateAccessorMethodsOfConceptClass(SchemaWithValidAnnotatedAccessorMethods.OneConcept::class)
    }

    @Schema(concepts = [ SchemaWithValidConceptIdAccessorAnnotatedAccessorMethods.OneConcept::class ])
    private interface SchemaWithValidConceptIdAccessorAnnotatedAccessorMethods {
        @Concept(facets = [])
        interface OneConcept {

            @QueryConceptId
            fun getConceptId(): QueryConceptId
        }
    }

    @Test
    fun `test concept with a valid annotated concept id accessor method should return without exception`() {
        ConceptQueryValidator.validateAccessorMethodsOfConceptClass(
            SchemaWithValidConceptIdAccessorAnnotatedAccessorMethods.OneConcept::class)
    }


    @Schema(concepts = [ ConceptWithUnsupportedFacet.OneConcept::class ])
    private interface ConceptWithUnsupportedFacet {
        @Concept(facets = [OneConcept.OneFacet::class])
        interface OneConcept {

            @Facet(FacetType.TEXT)
            interface OneFacet

            @Facet(FacetType.TEXT)
            interface UnsupportedFacet

            @QueryFacet(UnsupportedFacet::class)
            fun getFacetValue(): List<Any>
        }
    }

    @Test
    fun `test concept with a unsupported facet class should throw an exception`() {
        assertThrows(MalformedSchemaException::class.java) {
            ConceptQueryValidator.validateAccessorMethodsOfConceptClass(ConceptWithUnsupportedFacet.OneConcept::class)
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithValidFacets.OneConcept::class, SchemaWithConceptWithValidFacets.AnotherConcept::class ])
    private interface SchemaWithConceptWithValidFacets {
        interface CommonConceptInterface

        @Concept(facets = [])
        interface AnotherConcept: CommonConceptInterface


        @Concept(facets = [OneConcept.OneFacet::class])
        interface OneConcept: CommonConceptInterface {

            @Facet(FacetType.TEXT)
            interface OneFacet

            @QueryFacet(facetClass = OneFacet::class)
            fun getMyConceptsAsListOfAny(): List<Any>

            @QueryFacet(facetClass = OneFacet::class)
            fun getMyConceptsOfListOfConcreteConceptClass(): List<OneConcept>

            @QueryFacet(facetClass = OneFacet::class)
            fun getMyConceptsOfListWithACommonBaseInterface(): List<CommonConceptInterface>

            @QueryFacet(facetClass = OneFacet::class)
            fun getMyConceptsAsSetOfAny(): Set<Any>

            @QueryFacet(facetClass = OneFacet::class)
            fun getMyConceptsOfSetOfConcreteConceptClass(): Set<OneConcept>

            @QueryFacet(facetClass = OneFacet::class)
            fun getMyConceptsOfSetWithACommonBaseInterface(): Set<CommonConceptInterface>

            @QueryFacet(facetClass = OneFacet::class)
            fun getMyConceptsAsAny(): Any

            @QueryFacet(facetClass = OneFacet::class)
            fun getMyConceptsConcreteConceptClass(): OneConcept

            @QueryFacet(facetClass = OneFacet::class)
            fun getMyConceptsWithACommonBaseInterface(): CommonConceptInterface

            @QueryFacet(facetClass = OneFacet::class)
            fun getMyConceptsAsAnyNullable(): Any?

            @QueryFacet(facetClass = OneFacet::class)
            fun getMyConceptsConcreteConceptClassNullable(): OneConcept?

            @QueryFacet(facetClass = OneFacet::class)
            fun getMyConceptsWithACommonBaseInterfaceNullable(): CommonConceptInterface?
        }
    }

    @Test
    fun `test concept with valid return types should return without exception`() {
        ConceptQueryValidator.validateAccessorMethodsOfConceptClass(SchemaWithConceptWithValidFacets.OneConcept::class)
    }

    @Schema(concepts = [ SchemaWithFacetMethodHavingParameter.OneConceptClass::class ])
    private interface SchemaWithFacetMethodHavingParameter {

        @Concept(facets = [OneConceptClass.OneFacet::class])
        interface OneConceptClass {

            @Facet(FacetType.TEXT)
            interface OneFacet

            @QueryFacet(facetClass = OneFacet::class)
            fun getMyConceptsAsListOfAny(myParam: Int): List<Any>
        }
    }

    @Test
    fun `test concept with method having parameters should throw an exception`() {
        assertThrows(MalformedSchemaException::class.java) {
            ConceptQueryValidator.validateAccessorMethodsOfConceptClass(SchemaWithFacetMethodHavingParameter.OneConceptClass::class)
        }
    }

}