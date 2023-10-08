package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.FacetTypeEnum
import org.codeblessing.sourceamazing.api.process.schema.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcepts
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SchemaCreatorFacetTest {

    @Schema
    interface SchemaDefinitionWithDifferentTypes {
        @ChildConcepts(DifferentTypeConcept::class)
        fun getChildrenConcepts(): List<DifferentTypeConcept>

    }

    @Concept("DifferentTypeConcept")
    interface DifferentTypeConcept {

        @Facet("TextFacet")
        fun getTextFacet(): String

        @Facet("EnumerationFacet")
        fun getEnumerationFacet(): SeasonEnumeration

        @Facet("NumberFacet")
        fun getNumberFacet(): Int

        @Facet("BooleanFacet")
        fun getBooleanFacet(): Boolean

        @Facet("SelfReferenceFacet")
        fun getSelfReferenceFacet(): DifferentTypeConcept

        @Facet("OtherConceptReferenceFacet")
        fun getOtherConceptReferenceFacet(): OtherConcept

    }

    enum class SeasonEnumeration {
        WINTER,
        SPRING,
        SUMMER,
        FALL,
    }

    @Concept("OtherConcept")
    interface OtherConcept


        @Test
    fun `test valid concept with different types`() {
        val textFacetName = FacetName.of("TextFacet")
        val enumerationFacetName = FacetName.of("EnumerationFacet")
        val numberFacetName = FacetName.of("NumberFacet")
        val booleanFacetName = FacetName.of("BooleanFacet")
        val selfReferenceFacetName = FacetName.of("SelfReferenceFacet")
        val otherConceptReferenceFacetName = FacetName.of("OtherConceptReferenceFacet")
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaDefinitionWithDifferentTypes::class.java)
        Assertions.assertEquals(1, schema.numberOfConcepts())
        val concept = schema.conceptByConceptName(ConceptName.of("DifferentTypeConcept"))


        Assertions.assertTrue(concept.hasFacet(textFacetName))
        Assertions.assertTrue(concept.hasFacet(enumerationFacetName))
        Assertions.assertTrue(concept.hasFacet(numberFacetName))
        Assertions.assertTrue(concept.hasFacet(booleanFacetName))
        Assertions.assertTrue(concept.hasFacet(selfReferenceFacetName))
        Assertions.assertTrue(concept.hasFacet(otherConceptReferenceFacetName))

        Assertions.assertEquals(FacetTypeEnum.TEXT, concept.facetByName(textFacetName).facetType)
        Assertions.assertEquals(true, concept.facetByName(textFacetName).mandatory)

        Assertions.assertEquals(FacetTypeEnum.TEXT_ENUMERATION, concept.facetByName(enumerationFacetName).facetType)
        Assertions.assertEquals(true, concept.facetByName(enumerationFacetName).mandatory)
        Assertions.assertEquals(SeasonEnumeration::class, concept.facetByName(enumerationFacetName).enumerationType)

        Assertions.assertEquals(FacetTypeEnum.NUMBER, concept.facetByName(numberFacetName).facetType)
        Assertions.assertEquals(true, concept.facetByName(numberFacetName).mandatory)

        Assertions.assertEquals(FacetTypeEnum.BOOLEAN, concept.facetByName(booleanFacetName).facetType)
        Assertions.assertEquals(true, concept.facetByName(booleanFacetName).mandatory)

        Assertions.assertEquals(FacetTypeEnum.REFERENCE, concept.facetByName(selfReferenceFacetName).facetType)
        Assertions.assertEquals(true, concept.facetByName(selfReferenceFacetName).mandatory)

        Assertions.assertEquals(FacetTypeEnum.REFERENCE, concept.facetByName(otherConceptReferenceFacetName).facetType)
        Assertions.assertEquals(true, concept.facetByName(otherConceptReferenceFacetName).mandatory)

    }

    @Schema
    interface SchemaConceptWithDuplicateFacetsDefinitionClass {
        @ChildConcepts(ConceptWithWrongFacetType::class)
        fun getChildrenConcepts(): List<ConceptWithWrongFacetType>

    }

    @Concept("ConceptWithDuplicateFacets")
    interface ConceptWithDuplicateFacets {
        @Facet("MyDuplicateFacet")
        fun getDuplicateFacet(): String

        @Facet("MyDuplicateFacet")
        fun getSecondDuplicateFacet(): String
    }

    @Test
    fun `test concept with duplicate facet names should throw an exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaConceptWithDuplicateFacetsDefinitionClass::class.java)
        }
    }

    @Schema
    interface SchemaConceptWithWrongFacetTypeDefinitionClass {
        @ChildConcepts(ConceptWithWrongFacetType::class)
        fun getChildrenConcepts(): List<ConceptWithWrongFacetType>

    }

    @Concept("ConceptWithWrongFacetType")
    interface ConceptWithWrongFacetType {
        @Facet("MyFacetWithWrongType")
        fun getWrongTypeFacet(): Double
    }

    @Test
    fun `test concept with wrong facet type should throw an exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaConceptWithWrongFacetTypeDefinitionClass::class.java)
        }
    }

    @Schema
    interface SchemaConceptWithMandatoryFacetsAndOptionalFacetsDefinitionClass {
        @ChildConcepts(ConceptWithMandatoryAndOptionalFacets::class)
        fun getChildrenConcepts(): List<ConceptWithMandatoryAndOptionalFacets>

    }

    @Concept("ConceptWithMandatoryAndOptionalFacets")
    interface ConceptWithMandatoryAndOptionalFacets {
        @Facet(facetName = "DefaultMandatoryFacet")
        fun getDefaultMandatoryFacet(): Boolean

        @Facet(facetName = "MandatoryFacet", mandatory = true)
        fun getMandatoryFacet(): Int

        @Facet(facetName = "OptionalFacet", mandatory = false)
        fun getOptionalFacet(): Boolean
    }

    @Test
    fun `test valid concept with mandatory and optional facets`() {
        val defaultMandatoryFacetName = FacetName.of("DefaultMandatoryFacet")
        val mandatoryFacetName = FacetName.of("MandatoryFacet")
        val optionalFacetName = FacetName.of("OptionalFacet")
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaConceptWithMandatoryFacetsAndOptionalFacetsDefinitionClass::class.java)
        Assertions.assertEquals(1, schema.numberOfConcepts())
        val concept = schema.conceptByConceptName(ConceptName.of("ConceptWithMandatoryAndOptionalFacets"))

        Assertions.assertTrue(concept.hasFacet(defaultMandatoryFacetName))
        Assertions.assertTrue(concept.hasFacet(mandatoryFacetName))
        Assertions.assertTrue(concept.hasFacet(optionalFacetName))

        Assertions.assertEquals(true, concept.facetByName(defaultMandatoryFacetName).mandatory)
        Assertions.assertEquals(true, concept.facetByName(mandatoryFacetName).mandatory)
        Assertions.assertEquals(false, concept.facetByName(optionalFacetName).mandatory)
    }

    @Schema
    interface DifferentTypeDuplicateFacetSchemaDefinitionClass {
        @ChildConcepts(DifferentTypeDuplicateFacetConcept::class)
        fun getDuplicateFacetChildrenConcepts(): List<DifferentTypeDuplicateFacetConcept>

    }

    @Concept("DifferentTypeDuplicateFacet")
    interface DifferentTypeDuplicateFacetConcept {
        @Facet("Bar")
        fun getBarFacet(): Int

        @Facet("Bar")
        fun getAnotherBarFacet(): String

    }

    @Test
    fun `test with duplicate facet having different types concept`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(DifferentTypeDuplicateFacetSchemaDefinitionClass::class.java)
        }
    }

    @Schema
    interface DuplicateFacetSchemaDefinitionClass {
        @ChildConcepts(DuplicateFacetConcept::class)
        fun getDuplicateFacetChildrenConcepts(): List<DuplicateFacetConcept>

    }

    @Concept("DuplicateFacet")
    interface DuplicateFacetConcept {
        @Facet("Bar")
        fun getBarFacet(): String

        @Facet("Bar")
        fun getAnotherBarFacet(): String

    }

    @Test
    fun `test with same duplicate facet concept`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(DuplicateFacetSchemaDefinitionClass::class.java)
        Assertions.assertEquals(1, schema.numberOfConcepts())
        val fooConcept = schema.conceptByConceptName(ConceptName.of("DuplicateFacet"))
        Assertions.assertEquals(ConceptName.of("DuplicateFacet"), fooConcept.conceptName)
        Assertions.assertTrue(fooConcept.facetNames.contains(FacetName.of("Bar")))
        Assertions.assertEquals(1, fooConcept.facetNames.size)
    }


}
