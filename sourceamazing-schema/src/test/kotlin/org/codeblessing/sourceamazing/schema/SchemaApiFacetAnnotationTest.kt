package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.exceptions.MissingAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.NotInterfaceSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongClassStructureSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongTypeSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.DuplicateFacetSchemaSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongCardinalitySchemaSyntaxException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SchemaApiFacetAnnotationTest {

    @Schema(concepts = [SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class])
    private interface SchemaWithConceptWithTextFacet {
        @Concept(facets = [ConceptWithTextFacet.TextFacet::class])
        interface ConceptWithTextFacet {
            @StringFacet
            interface TextFacet
        }
    }

    @Test
    fun `test create an schema with concept class having a text facet should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) {
            // do nothing
        }
    }

    @Schema(concepts = [SchemaWithConceptWithValidEnumFacet.ConceptWitValidEnumFacet::class])
    private interface SchemaWithConceptWithValidEnumFacet {
        @Concept(facets = [
            ConceptWitValidEnumFacet.ValidEnumFacet::class,
        ])
        interface ConceptWitValidEnumFacet {
            @EnumFacet(enumerationClass = MyValidEnumeration::class)
            interface ValidEnumFacet
        }

        enum class MyValidEnumeration
    }

    @Test
    fun `test concept having an enumeration facet with a valid enumeration type should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithValidEnumFacet::class) {
            // do nothing
        }
    }

    @Schema(concepts = [SchemaAndConceptWithUnannotatedFacet.ConceptWithUnannotatedFacet::class])
    private interface SchemaAndConceptWithUnannotatedFacet {
        @Concept(facets = [ConceptWithUnannotatedFacet.UnannotatedFacet::class])
        interface ConceptWithUnannotatedFacet {
            interface UnannotatedFacet
        }
    }

    @Test
    fun `test create a concept with an unannotated facet should throw an exception`() {
        assertThrows(MissingAnnotationSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaAndConceptWithUnannotatedFacet::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaAndConceptWithFacetHavingMembers.ConceptWithFacetHavingMembers::class])
    private interface SchemaAndConceptWithFacetHavingMembers {
        @Concept(facets = [ConceptWithFacetHavingMembers.FacetHavingMembers::class])
        interface ConceptWithFacetHavingMembers {
            @StringFacet
            interface FacetHavingMembers {
                @Suppress("UNUSED")
                fun oneMemberOnFacetInterface()
            }
        }
    }

    @Test
    fun `test create a concept with an facet having members on it should throw an exception`() {
        assertThrows(WrongClassStructureSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaAndConceptWithFacetHavingMembers::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaAndConceptWithFacetClassInsteadOfInterface.ConceptWithFacetClassInsteadOfInterface::class])
    private interface SchemaAndConceptWithFacetClassInsteadOfInterface {
        @Concept(facets = [ConceptWithFacetClassInsteadOfInterface.FacetClassInsteadOfInterface::class])
        interface ConceptWithFacetClassInsteadOfInterface {
            @StringFacet
            class FacetClassInsteadOfInterface
        }
    }

    @Test
    fun `test create a concept with an facet class instead of interface should throw an exception`() {
        assertThrows(NotInterfaceSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaAndConceptWithFacetClassInsteadOfInterface::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaAndConceptWithFacetEnumInsteadOfInterface.ConceptWithFacetEnumInsteadOfInterface::class])
    private interface SchemaAndConceptWithFacetEnumInsteadOfInterface {
        @Concept(facets = [ConceptWithFacetEnumInsteadOfInterface.FacetEnumInsteadOfInterface::class])
        interface ConceptWithFacetEnumInsteadOfInterface {
            @StringFacet
            enum class FacetEnumInsteadOfInterface
        }
    }

    @Test
    fun `test create a concept with an facet enum instead of interface should throw an exception`() {
        assertThrows(NotInterfaceSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaAndConceptWithFacetEnumInsteadOfInterface::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaAndConceptWithFacetEnumInsteadOfInterface.ConceptWithFacetEnumInsteadOfInterface::class])
    private interface SchemaAndConceptWithFacetObjectInsteadOfInterface {
        @Concept(facets = [ConceptWithFacetObjectInsteadOfInterface.FacetObjectInsteadOfInterface::class])
        interface ConceptWithFacetObjectInsteadOfInterface {
            @StringFacet
            object FacetObjectInsteadOfInterface
        }
    }

    @Test
    fun `test create a concept with an facet object instead of interface should throw an exception`() {
        assertThrows(NotInterfaceSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaAndConceptWithFacetObjectInsteadOfInterface::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaAndConceptWithFacetEnumInsteadOfInterface.ConceptWithFacetEnumInsteadOfInterface::class])
    private interface SchemaAndConceptWithFacetAnnotationInterfaceInsteadOfInterface {
        @Concept(facets = [ConceptWithFacetAnnotationInterfaceInsteadOfInterface.FacetAnnotationInterfaceInsteadOfInterface::class])
        interface ConceptWithFacetAnnotationInterfaceInsteadOfInterface {
            @StringFacet
            annotation class FacetAnnotationInterfaceInsteadOfInterface
        }
    }

    @Test
    fun `test create a concept with an facet annotation interface instead of interface should throw an exception`() {
        assertThrows(NotInterfaceSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaAndConceptWithFacetAnnotationInterfaceInsteadOfInterface::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaAndConceptWithFacetHavingSchemaAnnotation.ConceptWithSchemaAnnotatedFacet::class])
    private interface SchemaAndConceptWithFacetHavingSchemaAnnotation {
        @Concept(facets = [ConceptWithSchemaAnnotatedFacet.SchemaAnnotatedFacet::class])
        interface ConceptWithSchemaAnnotatedFacet {
            @Schema(concepts = [])
            @StringFacet
            interface SchemaAnnotatedFacet
        }
    }

    @Test
    fun `test create facet class with a schema annotation should throw an exception`() {
        assertThrows(WrongAnnotationSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaAndConceptWithFacetHavingSchemaAnnotation::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaAndConceptWithFacetHavingMultipleFacetAnnotation.ConceptWithMultipleAnnotatedFacet::class])
    private interface SchemaAndConceptWithFacetHavingMultipleFacetAnnotation {
        @Concept(facets = [ConceptWithMultipleAnnotatedFacet.MultipleAnnotatedFacet::class])
        interface ConceptWithMultipleAnnotatedFacet {
            @StringFacet
            @IntFacet
            interface MultipleAnnotatedFacet
        }
    }

    @Test
    fun `test create facet with multiple facet annotations should throw an exception`() {
        assertThrows(WrongAnnotationSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaAndConceptWithFacetHavingMultipleFacetAnnotation::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaAndConceptWithFacetHavingConceptAnnotation.ConceptWithConceptAnnotatedFacet::class])
    private interface SchemaAndConceptWithFacetHavingConceptAnnotation {
        @Concept(facets = [ConceptWithConceptAnnotatedFacet.ConceptAnnotatedFacet::class])
        interface ConceptWithConceptAnnotatedFacet {
            @Concept(facets = [])
            @StringFacet
            interface ConceptAnnotatedFacet
        }
    }

    @Test
    fun `test create facet class with a concept annotation should throw an exception`() {
        assertThrows(WrongAnnotationSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaAndConceptWithFacetHavingConceptAnnotation::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaAndConceptWithDuplicateFacet.ConceptWithDuplicateFacets::class])
    private interface SchemaAndConceptWithDuplicateFacet {
        @Concept(facets = [
            ConceptWithDuplicateFacets.OneTextFacet::class,
            ConceptWithDuplicateFacets.OneTextFacet::class,
        ])
        interface ConceptWithDuplicateFacets {
            @StringFacet
            interface OneTextFacet
        }
    }

    @Test
    fun `test duplicate facet class within a concept should throw an exception`() {
        assertThrows(DuplicateFacetSchemaSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaAndConceptWithDuplicateFacet::class) {
                // do nothing
            }
        }
    }


    @Schema(concepts = [SchemaWithConceptWithNegativeCardinalityFacet.ConceptWithNegativeCardinalityFacet::class])
    private interface SchemaWithConceptWithNegativeCardinalityFacet {
        @Concept(facets = [ConceptWithNegativeCardinalityFacet.FacetWithNegativeCardinality::class])
        interface ConceptWithNegativeCardinalityFacet {
            @StringFacet(minimumOccurrences = -1, maximumOccurrences = 1)
            interface FacetWithNegativeCardinality
        }
    }

    @Test
    fun `test negative cardinality on facet should throw an exception`() {
        // we here only test this for minimumOccurrences and for the String facet
        assertThrows(WrongCardinalitySchemaSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithNegativeCardinalityFacet::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaWithConceptWithSwappedCardinalityFacet.ConceptWithSwappedCardinalityFacet::class])
    private interface SchemaWithConceptWithSwappedCardinalityFacet {
        @Concept(facets = [ConceptWithSwappedCardinalityFacet.SwappedCardinalityFacet::class])
        interface ConceptWithSwappedCardinalityFacet {
            @StringFacet(minimumOccurrences = 3, maximumOccurrences = 2)
            interface SwappedCardinalityFacet
        }
    }

    @Test
    fun `test min cardinality is greater than maximum cardinality on facet should throw an exception`() {
        // we here only test this for the String facet
        assertThrows(WrongCardinalitySchemaSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithSwappedCardinalityFacet::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaWithConceptWithInvalidEnumFacet.ConceptWithInvalidEnumFacet::class])
    private interface SchemaWithConceptWithInvalidEnumFacet {
        @Concept(facets = [
            ConceptWithInvalidEnumFacet.InvalidEnumFacet::class,
        ])
        interface ConceptWithInvalidEnumFacet {
            @EnumFacet(enumerationClass = String::class)
            interface InvalidEnumFacet
        }
    }

    @Test
    fun `test invalid enum type on facet should throw an exception`() {
        assertThrows(WrongTypeSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithInvalidEnumFacet::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaWithConceptWithUnitEnumTypeOnFacet.ConceptWithUnitEnumFacet::class])
    private interface SchemaWithConceptWithUnitEnumTypeOnFacet {
        @Concept(facets = [
            ConceptWithUnitEnumFacet.UnitEnumTypeFacet::class,
        ])
        interface ConceptWithUnitEnumFacet {
            @EnumFacet(enumerationClass = Unit::class)
            interface UnitEnumTypeFacet
        }
    }

    @Test
    fun `test enum facet with unit enum type should throw an exception`() {
        assertThrows(WrongTypeSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithUnitEnumTypeOnFacet::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaWithConceptWithEmptyReferenceFacet.ConceptWithEmptyReferenceFacet::class])
    private interface SchemaWithConceptWithEmptyReferenceFacet {
        @Concept(facets = [
            ConceptWithEmptyReferenceFacet.EmptyReferenceFacet::class,
        ])
        interface ConceptWithEmptyReferenceFacet {
            @ReferenceFacet(referencedConcepts = [])
            interface EmptyReferenceFacet
        }
    }

    @Test
    fun `test concept having an empty reference facet should throw an exception`() {
        assertThrows(WrongTypeSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithEmptyReferenceFacet::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaWithConceptWithUnknownReferencedConceptFacet.ConceptWithUnknownReferencedConceptFacet::class])
    private interface SchemaWithConceptWithUnknownReferencedConceptFacet {
        @Concept(facets = [
            ConceptWithUnknownReferencedConceptFacet.ReferenceFacetToUnknownConcept::class,
        ])
        interface ConceptWithUnknownReferencedConceptFacet {
            @ReferenceFacet(referencedConcepts = [OtherConcept::class])
            interface ReferenceFacetToUnknownConcept
        }

        @Concept(facets = [])
        interface OtherConcept // valid concept but not listed in schema
    }

    @Test
    fun `test reference facet to unknown concept should throw an exception`() {
        assertThrows(WrongTypeSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithUnknownReferencedConceptFacet::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaWithConceptWithReferenceToNonConceptClass.ConceptWithReferenceToNonConceptClass::class])
    private interface SchemaWithConceptWithReferenceToNonConceptClass {
        @Concept(facets = [
            ConceptWithReferenceToNonConceptClass.ReferenceFacetToNonConceptClass::class,
        ])
        interface ConceptWithReferenceToNonConceptClass {
            @ReferenceFacet(referencedConcepts = [OtherConceptWithoutConceptAnnotation::class])
            interface ReferenceFacetToNonConceptClass
        }

        interface OtherConceptWithoutConceptAnnotation
    }

    @Test
    fun `test reference facet to a non-concept class should throw an exception`() {
        assertThrows(WrongTypeSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithReferenceToNonConceptClass::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [SchemaAndConceptWithFacetWithTypeParameter.ConceptWithFacetWithTypeParameter::class])
    private interface SchemaAndConceptWithFacetWithTypeParameter {
        @Concept(facets = [ConceptWithFacetWithTypeParameter.FacetWithTypeParameter::class])
        interface ConceptWithFacetWithTypeParameter {
            @Suppress("UNUSED")
            @StringFacet
            interface FacetWithTypeParameter<T>
        }
    }

    @Test
    fun `test facet with type parameter should throw an exception`() {
        assertThrows(WrongTypeSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaAndConceptWithFacetWithTypeParameter::class) {
                // do nothing
            }
        }
    }

}