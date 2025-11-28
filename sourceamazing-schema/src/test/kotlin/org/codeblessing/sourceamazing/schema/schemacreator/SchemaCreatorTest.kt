package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.EnumFacetSchema
import org.codeblessing.sourceamazing.schema.api.FacetName
import org.codeblessing.sourceamazing.schema.api.FacetType
import org.codeblessing.sourceamazing.schema.api.ReferenceFacetSchema
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@Suppress("UNUSED", "Unused")
class SchemaCreatorTest {

    private interface EmptySchemaDefinitionClass

    @Test
    fun `test create an empty schema from an empty schema interface`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(EmptySchemaDefinitionClass::class)
        assertEquals(1, schema.numberOfConcepts())
    }

    private interface SchemaWithEmptyConceptClass {

        interface EmptyConceptClass

        val oneConcept: EmptyConceptClass
    }

    @Test
    fun `test create an schema with an empty concept class`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithEmptyConceptClass::class)
        assertEquals(2, schema.numberOfConcepts())
    }

    private interface SchemaWithConceptWithTextFacetClass {
        val myText: String
    }

    @Test
    fun `test create an schema with concept class having a text facet`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithTextFacetClass::class)
        assertEquals(1, schema.numberOfConcepts())
        val concept = schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithTextFacetClass::class))
        assertTrue(concept.hasFacet(FacetName.of(SchemaWithConceptWithTextFacetClass::myText.name)))
    }

    private interface SchemaWithConceptWithCorrectCardinalityFacets {
        val myText: String?

        val myBoolean: Boolean

        val myNumbers: Set<Int>
    }

    @Test
    fun `test concept having three facets with correct cardinalities`() {
        val myTextFacetName = FacetName.of(SchemaWithConceptWithCorrectCardinalityFacets::myText.name)
        val myBooleanFacetName = FacetName.of(SchemaWithConceptWithCorrectCardinalityFacets::myBoolean.name)
        val myNumbersFacetName = FacetName.of(SchemaWithConceptWithCorrectCardinalityFacets::myNumbers.name)
        val schema =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithCorrectCardinalityFacets::class)
        val conceptSchema =
            schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithCorrectCardinalityFacets::class))

        assertEquals(0, conceptSchema.facetByName(myTextFacetName).minimumOccurrences)
        assertEquals(1, conceptSchema.facetByName(myTextFacetName).maximumOccurrences)

        assertEquals(1, conceptSchema.facetByName(myBooleanFacetName).minimumOccurrences)
        assertEquals(1, conceptSchema.facetByName(myBooleanFacetName).maximumOccurrences)

        assertEquals(0, conceptSchema.facetByName(myNumbersFacetName).minimumOccurrences)
        assertEquals(Int.MAX_VALUE, conceptSchema.facetByName(myNumbersFacetName).maximumOccurrences)
    }

    private interface SchemaWithConceptWithEmptyEnumFacet {
        enum class EmptyEnumeration

        val myEnum: EmptyEnumeration
    }

    @Test
    fun `test concept having an empty enumeration facet should not throw an exception`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithEmptyEnumFacet::class)

        val conceptSchema = schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithEmptyEnumFacet::class))
        val enumFacetName = FacetName.of(SchemaWithConceptWithEmptyEnumFacet::myEnum.name)
        val enumFacetSchema = conceptSchema.facetByName(enumFacetName) as EnumFacetSchema
        assertEquals(enumFacetName, enumFacetSchema.facetName)
        assertEquals(FacetType.TEXT_ENUMERATION, enumFacetSchema.facetType)
        assertEquals(SchemaWithConceptWithEmptyEnumFacet.EmptyEnumeration::class, enumFacetSchema.enumerationType)
        assertEquals(0, enumFacetSchema.enumerationValues.size)
    }

    private interface SchemaWithConceptWithPrimitiveFacetClasses {
        val myText: String

        val myBoolean: Boolean

        val myNumber: Int
    }

    @Test
    fun `test concept having three primitive type facet`() {
        val schema =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithPrimitiveFacetClasses::class)
        assertEquals(1, schema.numberOfConcepts())
        val conceptSchema =
            schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithPrimitiveFacetClasses::class))
        assertEquals(3, conceptSchema.facets.size)

        val textFacetName = FacetName.of(SchemaWithConceptWithPrimitiveFacetClasses::myText.name)
        val booleanFacetName = FacetName.of(SchemaWithConceptWithPrimitiveFacetClasses::myBoolean.name)
        val numberFacetName = FacetName.of(SchemaWithConceptWithPrimitiveFacetClasses::myNumber.name)

        assertEquals(textFacetName, conceptSchema.facetByName(textFacetName).facetName)
        assertEquals(booleanFacetName, conceptSchema.facetByName(booleanFacetName).facetName)
        assertEquals(numberFacetName, conceptSchema.facetByName(numberFacetName).facetName)

        assertEquals(FacetType.TEXT, conceptSchema.facetByName(textFacetName).facetType)
        assertEquals(FacetType.BOOLEAN, conceptSchema.facetByName(booleanFacetName).facetType)
        assertEquals(FacetType.NUMBER, conceptSchema.facetByName(numberFacetName).facetType)
    }

    private interface SchemaWithConceptWithEnumFacet {
        val mySeasonEnum: SeasonEnumeration

        enum class SeasonEnumeration {
            WINTER,
            SPRING,
            SUMMER,
            FALL,
        }
    }

    @Test
    fun `test concept having a enumeration facet`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithEnumFacet::class)

        val conceptSchema = schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithEnumFacet::class))
        val enumFacetName = FacetName.of(SchemaWithConceptWithEnumFacet::mySeasonEnum.name)
        val enumFacetSchema = conceptSchema.facetByName(enumFacetName) as EnumFacetSchema
        assertEquals(enumFacetName, enumFacetSchema.facetName)
        assertEquals(FacetType.TEXT_ENUMERATION, enumFacetSchema.facetType)
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration::class, enumFacetSchema.enumerationType)
        assertEquals(4, enumFacetSchema.enumerationValues.size)
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.WINTER, enumFacetSchema.enumerationValues[0])
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.SPRING, enumFacetSchema.enumerationValues[1])
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.SUMMER, enumFacetSchema.enumerationValues[2])
        assertEquals(SchemaWithConceptWithEnumFacet.SeasonEnumeration.FALL, enumFacetSchema.enumerationValues[3])
    }

    private interface SchemaWithConceptWithReferenceFacet {
        val myReference: OtherConcept

        interface OtherConcept
    }

    @Test
    fun `test concept having a reference facet referencing one concept`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithReferenceFacet::class)

        val conceptSchema = schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithReferenceFacet::class))
        val referenceFacetName = FacetName.of(SchemaWithConceptWithReferenceFacet::myReference.name)
        val otherReferencedConceptName = ConceptName.of(SchemaWithConceptWithReferenceFacet.OtherConcept::class)
        val referenceFacetSchema = conceptSchema.facetByName(referenceFacetName) as ReferenceFacetSchema
        assertEquals(referenceFacetName, referenceFacetSchema.facetName)
        assertEquals(FacetType.REFERENCE, referenceFacetSchema.facetType)
        assertEquals(1, referenceFacetSchema.referencingConcepts.size)
        assertEquals(otherReferencedConceptName, referenceFacetSchema.referencingConcepts.first())
    }

    private interface SchemaWithConceptWithReferenceFacetToMultipleConcepts {
        @References([OtherConcept::class, AndAnotherConcept::class, AndJustOneAnotherConcept::class])
        val myReference: CommonInterface

        interface CommonInterface

        interface OtherConcept : CommonInterface

        interface AndAnotherConcept : CommonInterface

        interface AndJustOneAnotherConcept : CommonInterface
    }

    @Test
    fun `test concept having a reference facet referencing multiple concept`() {
        val schema =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(
                SchemaWithConceptWithReferenceFacetToMultipleConcepts::class
            )

        val conceptSchema =
            schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithReferenceFacetToMultipleConcepts::class))
        val referenceFacetName = FacetName.of(SchemaWithConceptWithReferenceFacetToMultipleConcepts::myReference.name)
        val referenceFacetSchema = conceptSchema.facetByName(referenceFacetName) as ReferenceFacetSchema

        val referencedConceptName1 =
            ConceptName.of(SchemaWithConceptWithReferenceFacetToMultipleConcepts.OtherConcept::class)
        val referencedConceptName2 =
            ConceptName.of(SchemaWithConceptWithReferenceFacetToMultipleConcepts.AndAnotherConcept::class)
        val referencedConceptName3 =
            ConceptName.of(SchemaWithConceptWithReferenceFacetToMultipleConcepts.AndJustOneAnotherConcept::class)

        assertEquals(referenceFacetName, referenceFacetSchema.facetName)
        assertEquals(FacetType.REFERENCE, referenceFacetSchema.facetType)
        assertEquals(3, referenceFacetSchema.referencingConcepts.size)
        assertTrue(referenceFacetSchema.referencingConcepts.contains(referencedConceptName1))
        assertTrue(referenceFacetSchema.referencingConcepts.contains(referencedConceptName2))
        assertTrue(referenceFacetSchema.referencingConcepts.contains(referencedConceptName3))
    }
}
