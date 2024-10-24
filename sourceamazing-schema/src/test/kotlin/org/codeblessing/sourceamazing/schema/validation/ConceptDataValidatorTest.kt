package org.codeblessing.sourceamazing.schema.validation

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataImpl
import org.codeblessing.sourceamazing.schema.datacollection.validation.ConceptDataValidator
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.DuplicateConceptIdentifierException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.MissingReferencedConceptFacetValueException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.UnknownConceptException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.UnknownFacetNameException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.WrongCardinalityForFacetValueException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.WrongReferencedConceptFacetValueException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.WrongTypeForFacetValueException
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class ConceptDataValidatorTest {

    @StringFacet(minimumOccurrences = 0, maximumOccurrences = 1)
    private interface OptionalTextFacetClass
    private val optionalTextFacetName = FacetName.of(OptionalTextFacetClass::class)

    @StringFacet(minimumOccurrences = 1, maximumOccurrences = 1)
    private interface MandatoryTextFacetClass
    private val mandatoryTextFacetName = FacetName.of(MandatoryTextFacetClass::class)

    @EnumFacet(minimumOccurrences = 1, maximumOccurrences = 5, enumerationClass = MyEnumeration::class)
    private interface SomeEnumsFacetClass
    private val someEnumsFacetName = FacetName.of(SomeEnumsFacetClass::class)

    @ReferenceFacet(minimumOccurrences = 1, maximumOccurrences = 1, referencedConcepts = [OtherConcept::class])
    private interface MandatoryReferenceToOtherConceptFacetClass
    private val mandatoryRefToOneConceptFacetName = FacetName.of(MandatoryReferenceToOtherConceptFacetClass::class)

    enum class MyEnumeration { @Suppress("UNUSED") X,@Suppress("UNUSED") Y, @Suppress("UNUSED") Z }
    enum class OtherEnumeration { @Suppress("UNUSED") X,@Suppress("UNUSED") Y, @Suppress("UNUSED") Z }
    enum class IncompatibleEnumeration { @Suppress("UNUSED") A,@Suppress("UNUSED") B, @Suppress("UNUSED") C }

    @Concept(facets = [
        OtherConceptTextFacetClass::class,
    ])
    interface OtherConcept

    @StringFacet(minimumOccurrences = 0, maximumOccurrences = 1)
    private interface OtherConceptTextFacetClass

    @Concept(facets = [])
    interface OtherThanTheOtherConcept


    @Schema(concepts = [
        SchemaForEmptyConceptValidation.EmptyConcept::class,
    ])
    private interface SchemaForEmptyConceptValidation {
        @Concept(facets = [])
        interface EmptyConcept

    }

    @Test
    fun `validate an empty list does return without exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForEmptyConceptValidation::class)
        ConceptDataValidator.validateEntries(schemaAccess, emptyList())
    }

    @Test
    fun `validate a unknown concept throws an exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForEmptyConceptValidation::class)
        val conceptData = createEmptyConceptData(OtherConcept::class) // here we define OtherConcept which is not known in this schema
        Assertions.assertThrows(UnknownConceptException::class.java) {
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
        }
    }

    @Test
    fun `validate a duplicate concept throws an exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForEmptyConceptValidation::class)
        val conceptDataOriginal = createEmptyConceptData(SchemaForEmptyConceptValidation.EmptyConcept::class)
        val conceptDataDuplicate = createEmptyConceptData(SchemaForEmptyConceptValidation.EmptyConcept::class)
        Assertions.assertThrows(DuplicateConceptIdentifierException::class.java) {
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptDataOriginal, conceptDataDuplicate))
        }
    }

    @Schema(concepts = [
        SchemaForOneMandatoryTextFacetValidation.ConceptClassWithFacets::class,
    ])
    private interface SchemaForOneMandatoryTextFacetValidation {
        @Concept(facets = [
            MandatoryTextFacetClass::class,
        ])
        interface ConceptClassWithFacets

    }

    @Test
    fun `validate unknown facet throws an exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(
            SchemaForOneMandatoryTextFacetValidation::class)
        val conceptData = createEmptyConceptData(SchemaForOneMandatoryTextFacetValidation.ConceptClassWithFacets::class)
        conceptData.addFacetValue(mandatoryTextFacetName, "my text")
        conceptData.addFacetValue(optionalTextFacetName, "my text") // here we add values for an unknown facet
        Assertions.assertThrows(UnknownFacetNameException::class.java) {
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
        }
    }

    @Test
    fun `validate a valid entry does return without exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(
            SchemaForOneMandatoryTextFacetValidation::class)
        val conceptData = createEmptyConceptData(SchemaForOneMandatoryTextFacetValidation.ConceptClassWithFacets::class)
        conceptData.addFacetValue(mandatoryTextFacetName, "my text")
        ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
    }

    @Test
    fun `validate a entry with wrong type does throw an exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(
            SchemaForOneMandatoryTextFacetValidation::class)
        val conceptData = createEmptyConceptData(SchemaForOneMandatoryTextFacetValidation.ConceptClassWithFacets::class)
        conceptData.addFacetValue(mandatoryTextFacetName, 42) // here we add a number instead of text
        Assertions.assertThrows(WrongTypeForFacetValueException::class.java) {
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
        }
    }

    @Test
    fun `validate missing mandatory text facet throws an exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(
            SchemaForOneMandatoryTextFacetValidation::class)
        val conceptData = createEmptyConceptData(SchemaForOneMandatoryTextFacetValidation.ConceptClassWithFacets::class)
        // here we do not add the mandatory text facet
        Assertions.assertThrows(WrongCardinalityForFacetValueException::class.java) {
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
        }
    }

    @Test
    fun `validate too much values on facet throws an exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(
            SchemaForOneMandatoryTextFacetValidation::class)
        val conceptData = createEmptyConceptData(SchemaForOneMandatoryTextFacetValidation.ConceptClassWithFacets::class)
        conceptData.addFacetValue(mandatoryTextFacetName, "my text")
        conceptData.addFacetValue(mandatoryTextFacetName, "my text number two")
        Assertions.assertThrows(WrongCardinalityForFacetValueException::class.java) {
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
        }
    }

    @Schema(concepts = [
        SchemaForSomeEnumsFacetValidation.ConceptClassWithFacets::class,
    ])
    private interface SchemaForSomeEnumsFacetValidation {
        @Concept(facets = [
            SomeEnumsFacetClass::class,
        ])
        interface ConceptClassWithFacets

    }

    @Test
    fun `validate that a correct enum does return without exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForSomeEnumsFacetValidation::class)
        val conceptData = createEmptyConceptData(SchemaForSomeEnumsFacetValidation.ConceptClassWithFacets::class)
        conceptData.addFacetValue(someEnumsFacetName, MyEnumeration.X)
        conceptData.addFacetValue(someEnumsFacetName, MyEnumeration.Y)
        conceptData.addFacetValue(someEnumsFacetName, MyEnumeration.X)
        ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
    }

    @Test
    fun `validate that a correct enum as String does return without exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForSomeEnumsFacetValidation::class)
        val conceptData = createEmptyConceptData(SchemaForSomeEnumsFacetValidation.ConceptClassWithFacets::class)
        conceptData.addFacetValue(someEnumsFacetName, MyEnumeration.X)
        conceptData.addFacetValue(someEnumsFacetName, MyEnumeration.Y.toString())
        conceptData.addFacetValue(someEnumsFacetName, MyEnumeration.X.toString())
        ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
    }

    @Test
    fun `validate that a compatible enum type does return without exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForSomeEnumsFacetValidation::class)
        val conceptData = createEmptyConceptData(SchemaForSomeEnumsFacetValidation.ConceptClassWithFacets::class)
        conceptData.addFacetValue(someEnumsFacetName, MyEnumeration.X)
        conceptData.addFacetValue(someEnumsFacetName, OtherEnumeration.Y)
        ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
    }

    @Test
    fun `validate that a incompatible enum type throws an exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForSomeEnumsFacetValidation::class)
        val conceptData = createEmptyConceptData(SchemaForSomeEnumsFacetValidation.ConceptClassWithFacets::class)
        conceptData.addFacetValue(someEnumsFacetName, MyEnumeration.X)
        conceptData.addFacetValue(someEnumsFacetName, IncompatibleEnumeration.B)
        Assertions.assertThrows(WrongTypeForFacetValueException::class.java) {
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
        }
    }

    @Test
    fun `validate that a wrong enum type as string throws an exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForSomeEnumsFacetValidation::class)
        val conceptData = createEmptyConceptData(SchemaForSomeEnumsFacetValidation.ConceptClassWithFacets::class)
        conceptData.addFacetValue(someEnumsFacetName, MyEnumeration.X)
        conceptData.addFacetValue(someEnumsFacetName, "x") // lowercase is wrong
        Assertions.assertThrows(WrongTypeForFacetValueException::class.java) {
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
        }
    }

    @Schema(concepts = [
        SchemaForReferenceFacetValidation.ConceptClassWithFacets::class,
        OtherConcept::class,
        OtherThanTheOtherConcept::class,
    ])
    private interface SchemaForReferenceFacetValidation {
        @Concept(facets = [
            MandatoryReferenceToOtherConceptFacetClass::class,
        ])
        interface ConceptClassWithFacets

    }

    @Test
    fun `validate that a wrong type for reference throws an exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForReferenceFacetValidation::class)
        val conceptData = createEmptyConceptData(SchemaForReferenceFacetValidation.ConceptClassWithFacets::class)
        conceptData.addFacetValue(mandatoryRefToOneConceptFacetName, "Bar")
        Assertions.assertThrows(WrongTypeForFacetValueException::class.java) {
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
        }
    }

    @Test
    fun `validate that a reference pointing to a missing concept throws an exception`() {
        val conceptIdentifier = ConceptIdentifier.of("Bar")
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForReferenceFacetValidation::class)
        val conceptDataReferencing = createEmptyConceptData(SchemaForReferenceFacetValidation.ConceptClassWithFacets::class)
        conceptDataReferencing.addFacetValue(mandatoryRefToOneConceptFacetName, conceptIdentifier)

        Assertions.assertThrows(MissingReferencedConceptFacetValueException::class.java) {
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptDataReferencing))
        }
    }

    @Test
    fun `validate that a reference pointing to an available concept does return without exception`() {
        val conceptIdentifier = ConceptIdentifier.of("Bar")
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForReferenceFacetValidation::class)
        val conceptDataReferencing = createEmptyConceptData(SchemaForReferenceFacetValidation.ConceptClassWithFacets::class)
        conceptDataReferencing.addFacetValue(mandatoryRefToOneConceptFacetName, conceptIdentifier)
        val conceptDataReferenced = createEmptyConceptData(OtherConcept::class, conceptIdentifier)
        ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptDataReferencing, conceptDataReferenced))
    }

    @Test
    fun `validate that a reference pointing to an available concept with wrong type throws an exception`() {
        val conceptIdentifier = ConceptIdentifier.of("Bar")
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForReferenceFacetValidation::class)
        val conceptDataReferencing = createEmptyConceptData(SchemaForReferenceFacetValidation.ConceptClassWithFacets::class)
        conceptDataReferencing.addFacetValue(mandatoryRefToOneConceptFacetName, conceptIdentifier)
        val conceptDataReferenced = createEmptyConceptData(OtherThanTheOtherConcept::class, conceptIdentifier) // wrong concept type
        Assertions.assertThrows(WrongReferencedConceptFacetValueException::class.java) {
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptDataReferencing, conceptDataReferenced))
        }

    }

    private fun createEmptyConceptData(conceptClass: KClass<*>, conceptIdentifier: ConceptIdentifier = ConceptIdentifier.of("Foo")): ConceptDataImpl {
        return ConceptDataImpl(
            1,
            ConceptName.of(conceptClass),
            conceptIdentifier
        )

    }
}