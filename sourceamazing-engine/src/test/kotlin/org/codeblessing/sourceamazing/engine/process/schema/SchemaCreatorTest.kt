package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.exceptions.MalformedSchemaException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class SchemaCreatorTest {
    interface UnannotatedSchemaDefinitionClass

    @Test
    fun `test unannotated class should throw an exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(UnannotatedSchemaDefinitionClass::class.java)
        }
    }

    @Schema
    class NonInterfaceSchemaDefinitionClass

    @Test
    fun `test non-interface class should throw an exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(NonInterfaceSchemaDefinitionClass::class.java)
        }
    }

    @Schema
    interface EmptySchemaDefinitionClass

    @Test
    fun `test create an empty schema from an empty schema interface`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(EmptySchemaDefinitionClass::class.java)
        Assertions.assertEquals(0, schema.numberOfConcepts())
    }

    @Schema
    interface SimpleSchemaDefinitionClass {
        @ChildConcepts(SimpleConcept::class)
        fun getChildrenConceptsOfFoo(): List<SimpleConcept>

    }

    @Concept("FooConcept")
    interface SimpleConcept {
        @Facet("Bar")
        fun getBarFacet(): String

        @ConceptId
        fun theConceptIdentifier(): String

        @ConceptId
        fun theConceptIdentifierAsObject(): ConceptIdentifier
    }

    @Test
    fun `test with single root concept`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SimpleSchemaDefinitionClass::class.java)
        Assertions.assertEquals(1, schema.numberOfConcepts())
        val fooConcept = schema.conceptByConceptName(ConceptName.of("FooConcept"))
        Assertions.assertEquals(ConceptName.of("FooConcept"), fooConcept.conceptName)
        Assertions.assertNull(fooConcept.parentConceptName)
        Assertions.assertTrue(fooConcept.facetNames.contains(FacetName.of("Bar")))
    }


    @Schema
    interface WrongConceptIdentifierSchemaDefinitionClass {
        @ChildConcepts(WrongConceptIdentifierConcept::class)
        fun getChildrenConcepts(): List<WrongConceptIdentifierConcept>

    }

    @Concept("WrongConceptIdentifier")
    interface WrongConceptIdentifierConcept {
        @ConceptId
        fun getWrongTypedIdentifier(): Any

    }

    @Test
    fun `test with wrong concept identifier type`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(WrongConceptIdentifierSchemaDefinitionClass::class.java)
        }
    }

    @Schema
    interface InheritanceChildConceptsSchemaDefinitionClass {
        @ChildConceptsWithCommonBaseInterface(CommonChildConceptInterface::class, conceptClasses = [InheritanceOneChildConcept::class, InheritanceTwoChildConcept::class])
        fun getMultipleChildrenConcepts(): List<CommonChildConceptInterface>

    }
    interface CommonChildConceptInterface

    @Concept("InheritanceOneChildConcept")
    interface InheritanceOneChildConcept: CommonChildConceptInterface {}

    @Concept("InheritanceTwoChildConcept")
    interface InheritanceTwoChildConcept: CommonChildConceptInterface {}


    @Test
    fun `test with inheritance child concepts`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(InheritanceChildConceptsSchemaDefinitionClass::class.java)
        Assertions.assertEquals(2, schema.numberOfConcepts())
        val inheritanceOneChildConcept = schema.conceptByConceptName(ConceptName.of("InheritanceOneChildConcept"))
        Assertions.assertEquals(ConceptName.of("InheritanceOneChildConcept"), inheritanceOneChildConcept.conceptName)
        val inheritanceTwoChildConcept = schema.conceptByConceptName(ConceptName.of("InheritanceTwoChildConcept"))
        Assertions.assertEquals(ConceptName.of("InheritanceTwoChildConcept"), inheritanceTwoChildConcept.conceptName)
        Assertions.assertNull(inheritanceOneChildConcept.parentConceptName)
        Assertions.assertNull(inheritanceTwoChildConcept.parentConceptName)
    }

    @Schema
    interface IncompatibleInheritanceChildConceptsSchemaDefinitionClass {
        @ChildConceptsWithCommonBaseInterface(IncompatibleChildConceptInterface::class, conceptClasses = [IncompatibleInheritanceOneChildConcept::class, IncompatibleInheritanceTwoChildConcept::class])
        fun getMultipleChildrenConcepts(): List<IncompatibleChildConceptInterface>

    }
    interface IncompatibleChildConceptInterface

    @Concept("IncompatibleInheritanceOneChildConcept")
    interface IncompatibleInheritanceOneChildConcept: IncompatibleChildConceptInterface {}

    @Concept("IncompatibleInheritanceTwoChildConcept")
    interface IncompatibleInheritanceTwoChildConcept {}

    @Test
    fun `test with incompatible inheritance child concepts`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(IncompatibleInheritanceChildConceptsSchemaDefinitionClass::class.java)
        }
    }

    @Schema
    interface DuplicatedConceptInheritanceChildConceptsSchemaDefinitionClass {
        @ChildConceptsWithCommonBaseInterface(DuplicatedConceptChildConceptInterface::class, conceptClasses = [DuplicatedConceptInheritanceOneChildConcept::class, DuplicatedConceptInheritanceTwoChildConcept::class])
        fun getMultipleChildrenConcepts(): List<DuplicatedConceptChildConceptInterface>

    }
    @Concept("DuplicatedConceptInheritanceChildConcept")
    interface DuplicatedConceptChildConceptInterface

    @Concept("DuplicatedConceptInheritanceOneChildConcept")
    interface DuplicatedConceptInheritanceOneChildConcept: DuplicatedConceptChildConceptInterface {}

    @Concept("DuplicatedConceptInheritanceTwoChildConcept")
    interface DuplicatedConceptInheritanceTwoChildConcept {}

    @Test
    fun `test with duplicated concept inheritance child concepts`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(DuplicatedConceptInheritanceChildConceptsSchemaDefinitionClass::class.java)
        }
    }

    @Schema
    interface EmptyCommonChildConceptsSchemaDefinitionClass {
        @ChildConceptsWithCommonBaseInterface(EmptyCommonChildConceptInterface::class, conceptClasses = [])
        fun getMultipleChildrenConcepts(): List<EmptyCommonChildConceptInterface>

    }
    interface EmptyCommonChildConceptInterface

    @Test
    fun `test with base interface without inheritance child concepts`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(EmptyCommonChildConceptsSchemaDefinitionClass::class.java)
        Assertions.assertEquals(0, schema.numberOfConcepts())
    }

    @Schema
    interface SeparatelyDefinedChildConceptsSchemaDefinitionClass {

        @ChildConcepts(SeparatelyDefinedInheritanceOneChildConcept::class)
        fun getSeparatelyDefinedInheritanceOneChildConcept(): List<SeparatelyDefinedInheritanceOneChildConcept>

        @ChildConceptsWithCommonBaseInterface(SeparatelyDefinedCommonChildConceptInterface::class, conceptClasses = [SeparatelyDefinedInheritanceOneChildConcept::class, SeparatelyDefinedInheritanceTwoChildConcept::class])
        fun getMultipleChildrenConcepts(): List<SeparatelyDefinedCommonChildConceptInterface>

        @ChildConcepts(SeparatelyDefinedInheritanceTwoChildConcept::class)
        fun getSeparatelyDefinedInheritanceTwoChildConcept(): List<SeparatelyDefinedInheritanceTwoChildConcept>

    }
    interface SeparatelyDefinedCommonChildConceptInterface

    @Concept("SeparatelyDefinedInheritanceOneChildConcept")
    interface SeparatelyDefinedInheritanceOneChildConcept: SeparatelyDefinedCommonChildConceptInterface {}

    @Concept("SeparatelyDefinedInheritanceTwoChildConcept")
    interface SeparatelyDefinedInheritanceTwoChildConcept: SeparatelyDefinedCommonChildConceptInterface {}


    @Test
    fun `test with separately defined child concepts`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SeparatelyDefinedChildConceptsSchemaDefinitionClass::class.java)
        Assertions.assertEquals(2, schema.numberOfConcepts())
        val inheritanceOneChildConcept = schema.conceptByConceptName(ConceptName.of("SeparatelyDefinedInheritanceOneChildConcept"))
        Assertions.assertEquals(ConceptName.of("SeparatelyDefinedInheritanceOneChildConcept"), inheritanceOneChildConcept.conceptName)
        val inheritanceTwoChildConcept = schema.conceptByConceptName(ConceptName.of("SeparatelyDefinedInheritanceTwoChildConcept"))
        Assertions.assertEquals(ConceptName.of("SeparatelyDefinedInheritanceTwoChildConcept"), inheritanceTwoChildConcept.conceptName)
        Assertions.assertNull(inheritanceOneChildConcept.parentConceptName)
        Assertions.assertNull(inheritanceTwoChildConcept.parentConceptName)
    }


    @Schema
    interface SimpleSchemaDefinitionClassWithUnannotatedMethods {
        @ChildConcepts(SimpleConcept::class)
        fun getChildrenConceptsOfFoo(): List<SimpleConcept>

        fun getOtherChildren(): List<SimpleConcept>
    }

    @Test
    fun `test schema with unannotated methods should throw an exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SimpleSchemaDefinitionClassWithUnannotatedMethods::class.java)
        }
    }

    @Schema
    interface SimpleSchemaDefinitionClassWithFacetMethods {
        @ChildConcepts(SimpleConcept::class)
        fun getChildrenConceptsOfFoo(): List<SimpleConcept>

        @Facet("MyFacet")
        fun getStringValue(): String
    }

    @Test
    fun `test schema with facet methods should throw an exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SimpleSchemaDefinitionClassWithFacetMethods::class.java)
        }
    }

    @Schema
    interface SimpleSchemaDefinitionClassButNotACollection {
        @ChildConcepts(SimpleConcept::class)
        fun getChildrenConceptsOfFoo(): SimpleConcept

    }

    @Test
    fun `test schema with a child concept annotation returning a single entry should throw an exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SimpleSchemaDefinitionClassButNotACollection::class.java)
        }
    }

    @Schema
    interface SimpleSchemaDefinitionClassButNotAList {
        @ChildConcepts(SimpleConcept::class)
        fun getChildrenConceptsOfFoo(): Set<SimpleConcept>

    }

    @Test
    fun `test schema with a child concept annotation not returning a List should throw an exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SimpleSchemaDefinitionClassButNotAList::class.java)
        }
    }

    @Schema
    interface SimpleSchemaDefinitionClassButNotAListOfConcept {
        @ChildConcepts(NotConcept::class)
        fun getChildrenConceptsOfFoo(): List<NotConcept>

    }

    interface NotConcept
    @Test
    fun `test schema with a method not returning an interface annotated with @Concept should throw an exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SimpleSchemaDefinitionClassButNotAListOfConcept::class.java)
        }
    }

    @Schema
    interface NestedSchemaDefinitionClass {
        @ChildConcepts(SubConcept1::class)
        fun getChildrenConcepts1(): List<SubConcept1>

        @ChildConcepts(SubConcept2::class)
        fun getChildrenConcepts2(): List<SubConcept2>

    }

    @Concept("SubConcept1")
    interface SubConcept1 {
        @ChildConcepts(SubSubConcept1::class)
        fun getChildrenConceptsOfChild(): List<SubSubConcept1>

        @Facet("SubConcept1Facet1")
        fun getSubConcept1Facet1(): String

        @Facet("SubConcept1Facet2")
        fun getSubConcept1Facet2(): String


    }

    @Concept("SubSubConcept1")
    interface SubSubConcept1 {
        @ChildConcepts(SubSubSubConcept1::class)
        fun getChildrenConceptsOfChild(): List<SubSubSubConcept1>

    }


    @Concept("SubSubSubConcept1")
    interface SubSubSubConcept1

    @Concept("SubConcept2")
    interface SubConcept2 {
        @ChildConcepts(SubSubConcept2::class)
        fun getChildrenConceptsOfChild(): List<SubSubConcept2>

        @Facet("SubConcept2Facet")
        fun getSubConcept2Facet(): String


    }

    @Concept("SubSubConcept2")
    interface SubSubConcept2

    @Test
    fun `test schema with two nested concepts`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(NestedSchemaDefinitionClass::class.java)
        Assertions.assertEquals(5, schema.numberOfConcepts())

        val subConcept1 = schema.conceptByConceptName(ConceptName.of("SubConcept1"))
        Assertions.assertEquals(ConceptName.of("SubConcept1"), subConcept1.conceptName)
        Assertions.assertNull(subConcept1.parentConceptName)
        Assertions.assertEquals(2, subConcept1.facetNames.size)
        Assertions.assertTrue(subConcept1.facetNames.contains(FacetName.of("SubConcept1Facet1")))
        Assertions.assertTrue(subConcept1.facetNames.contains(FacetName.of("SubConcept1Facet2")))


        val subSubConcept1 = schema.conceptByConceptName(ConceptName.of("SubSubConcept1"))
        Assertions.assertEquals(ConceptName.of("SubSubConcept1"), subSubConcept1.conceptName)
        Assertions.assertEquals(ConceptName.of("SubConcept1"), subSubConcept1.parentConceptName)
        Assertions.assertEquals(0, subSubConcept1.facetNames.size)

        val subSubSubConcept1 = schema.conceptByConceptName(ConceptName.of("SubSubSubConcept1"))
        Assertions.assertEquals(ConceptName.of("SubSubSubConcept1"), subSubSubConcept1.conceptName)
        Assertions.assertEquals(ConceptName.of("SubSubConcept1"), subSubSubConcept1.parentConceptName)
        Assertions.assertEquals(0, subSubSubConcept1.facetNames.size)


        val subConcept2 = schema.conceptByConceptName(ConceptName.of("SubConcept2"))
        Assertions.assertEquals(ConceptName.of("SubConcept2"), subConcept2.conceptName)
        Assertions.assertNull(subConcept2.parentConceptName)
        Assertions.assertEquals(1, subConcept2.facetNames.size)
        Assertions.assertTrue(subConcept2.facetNames.contains(FacetName.of("SubConcept2Facet")))

        val subSubConcept2 = schema.conceptByConceptName(ConceptName.of("SubSubConcept2"))
        Assertions.assertEquals(ConceptName.of("SubSubConcept2"), subSubConcept2.conceptName)
        Assertions.assertEquals(ConceptName.of("SubConcept2"), subSubConcept2.parentConceptName)
        Assertions.assertEquals(0, subSubConcept2.facetNames.size)
    }

    @Schema
    interface DirectCyclicSchemaDefinitionClass {
        @ChildConcepts(DirectCyclicConcept::class)
        fun getCyclicChildrenConcept(): List<DirectCyclicConcept>

    }

    @Concept("DirectCyclicConcept")
    interface DirectCyclicConcept {
        @ChildConcepts(DirectCyclicConcept::class)
        fun getChildrenConcepts(): List<DirectCyclicConcept>

    }

    @Test
    fun `test schema with root schema inside a child concept throw exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(DirectCyclicSchemaDefinitionClass::class.java)
        }
    }

    @Schema
    interface TwoParentConceptForOneChildConceptSchemaDefinitionClass {
        @ChildConcepts(TwoParentConceptParent1Concept::class)
        fun getFirstParentConcept(): List<TwoParentConceptParent1Concept>

        @ChildConcepts(TwoParentConceptParent2Concept::class)
        fun getSecondParentConcept(): List<TwoParentConceptParent2Concept>

    }

    @Concept("TwoParentConceptParent1Concept")
    interface TwoParentConceptParent1Concept {
        @ChildConcepts(TwoParentConceptChildConcept::class)
        fun getChildrenConcepts(): List<TwoParentConceptChildConcept>

    }

    @Concept("TwoParentConceptParent2Concept")
    interface TwoParentConceptParent2Concept {
        @ChildConcepts(TwoParentConceptChildConcept::class)
        fun getChildrenConcepts(): List<TwoParentConceptChildConcept>

    }

    @Concept("TwoParentConceptChildConcept")
    interface TwoParentConceptChildConcept

    @Test
    fun `test schema with two parents concepts having same child concept throw exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(TwoParentConceptForOneChildConceptSchemaDefinitionClass::class.java)
        }
    }

    @Schema
    interface IndirectCyclicSchemaDefinitionClass {
        @ChildConcepts(IndirectCyclicConcept::class)
        fun getCyclicChildrenConcept(): List<IndirectCyclicConcept>

    }

    @Concept("IndirectCyclicConcept")
    interface IndirectCyclicConcept {
        @ChildConcepts(IndirectCyclicSubConcept::class)
        fun getSubChildrenConcepts(): List<IndirectCyclicSubConcept>

    }

    @Concept("IndirectCyclicSubConcept")
    interface IndirectCyclicSubConcept {
        @ChildConcepts(IndirectCyclicConcept::class)
        fun getChildrenConcepts(): List<IndirectCyclicConcept>

    }

    @Test
    fun `test multiple concepts in cyclic hierarchy throws exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(IndirectCyclicSchemaDefinitionClass::class.java)
        }
    }

    @Schema
    interface SchemaDefinitionWithDuplicateRootConceptClass {
        @ChildConcepts(DuplicateRootConcept::class)
        fun getChildrenConcepts(): List<DuplicateRootConcept>

        @ChildConcepts(DuplicateRootConcept::class)
        fun getAnotherChildrenConcepts(): List<DuplicateRootConcept>

    }

    @Concept("DuplicateRootConcept")
    interface DuplicateRootConcept

    @Test
    fun `test with duplicate root concept`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaDefinitionWithDuplicateRootConceptClass::class.java)
        Assertions.assertEquals(1, schema.numberOfConcepts())
        val concept = schema.conceptByConceptName(ConceptName.of("DuplicateRootConcept"))
        Assertions.assertEquals(ConceptName.of("DuplicateRootConcept"), concept.conceptName)
        Assertions.assertNull(concept.parentConceptName)
    }

    @Schema
    interface SchemaDefinitionWithParentAnnotationOnSchemaClass {
        @ChildConcepts(ParentAnnotationOnSchemaClassConcept::class)
        fun getChildrenConcept(): List<ParentAnnotationOnSchemaClassConcept>

        @ParentConcept
        fun getParent(): ParentAnnotationOnSchemaClassConcept

    }

    @Concept("ParentAnnotationOnSchemaClassConcept")
    interface ParentAnnotationOnSchemaClassConcept

    @Test
    fun `test with parent annotation on schema class`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaDefinitionWithParentAnnotationOnSchemaClass::class.java)
        }
    }

    @Schema
    interface SchemaDefinitionWithParentAnnotationOnTopLevelConcept {
        @ChildConcepts(ParentAnnotationOnTopLevelConcept::class)
        fun getChildrenConcept(): List<ParentAnnotationOnTopLevelConcept>

        @ChildConcepts(ParentAnnotationOnTopLevelSecondConcept::class)
        fun getSecondChildrenConcept(): List<ParentAnnotationOnTopLevelSecondConcept>


    }

    @Concept("ParentAnnotationOnTopLevelConcept")
    interface ParentAnnotationOnTopLevelConcept {
        @ParentConcept
        fun getParent(): ParentAnnotationOnTopLevelSecondConcept

    }

    @Concept("ParentAnnotationOnTopLevelSecondConcept")
    interface ParentAnnotationOnTopLevelSecondConcept {

    }


    @Test
    fun `test with parent annotation on top-level concept class`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaDefinitionWithParentAnnotationOnTopLevelConcept::class.java)
        }
    }

    @Schema
    interface SchemaDefinitionWithWrongParentConceptType {
        @ChildConcepts(ParentAnnotationWithWrongTypeParentConcept::class)
        fun getChildrenConcept(): List<ParentAnnotationWithWrongTypeParentConcept>

        @ChildConcepts(ParentAnnotationWithWrongTypeSecondParentConcept::class)
        fun getSecondChildrenConcept(): List<ParentAnnotationWithWrongTypeSecondParentConcept>


    }

    @Concept("ParentAnnotationWithWrongTypeSecondParentConcept")
    interface ParentAnnotationWithWrongTypeSecondParentConcept

    @Concept("ParentAnnotationWithWrongTypeParentConcept")
    interface ParentAnnotationWithWrongTypeParentConcept {
        @ChildConcepts(ParentAnnotationWithWrongTypeChildConcept::class)
        fun getChildrenConcept(): List<ParentAnnotationWithWrongTypeChildConcept>

    }

    @Concept("ParentAnnotationWithWrongTypeChildConcept")
    interface ParentAnnotationWithWrongTypeChildConcept {
        @ParentConcept
        fun getParent(): ParentAnnotationWithWrongTypeSecondParentConcept

    }

    @Test
    fun `test with parent annotation with wrong type`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaDefinitionWithWrongParentConceptType::class.java)
        }
    }

    @Schema
    interface SchemaDefinitionWithValidParentAnnotationOnChildLevelConcept {
        @ChildConcepts(ParentAnnotationOnTopLevelWithChildrenConcept::class)
        fun getChildrenConcept(): List<ParentAnnotationOnTopLevelWithChildrenConcept>

    }

    @Concept("ParentAnnotationOnTopLevelWithChildrenConcept")
    interface ParentAnnotationOnTopLevelWithChildrenConcept {
        @ChildConcepts(ParentAnnotationOnTopLevelChildConcept::class)
        fun getChildrenConcept(): List<ParentAnnotationOnTopLevelChildConcept>

    }

    @Concept("ParentAnnotationOnTopLevelChildConcept")
    interface ParentAnnotationOnTopLevelChildConcept {
        @ParentConcept
        fun getParent(): ParentAnnotationOnTopLevelWithChildrenConcept

    }


    @Test
    fun `test with parent annotation on child-level concept class`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaDefinitionWithValidParentAnnotationOnChildLevelConcept::class.java)
        Assertions.assertEquals(2, schema.numberOfConcepts())
        val concept = schema.conceptByConceptName(ConceptName.of("ParentAnnotationOnTopLevelChildConcept"))
        Assertions.assertEquals(ConceptName.of("ParentAnnotationOnTopLevelChildConcept"), concept.conceptName)
        Assertions.assertEquals(ConceptName.of("ParentAnnotationOnTopLevelWithChildrenConcept"), concept.parentConceptName)
    }

    @Schema
    @Concept("SchemaAndConceptDefinitionClass")
    interface SchemaAndConceptDefinitionClass


    @Test
    fun `test schema with root schema that is itself a concept throw exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaAndConceptDefinitionClass::class.java)
        }
    }

    @Schema
    interface SimpleSchemaDefinitionClassWithNullableListOfConcept {
        @ChildConcepts(SimpleConcept::class)
        fun getChildrenConcepts(): List<SimpleConcept>?

    }

    @Test
    @Disabled("Not working without kotlin reflection")
    fun `test schema with a method returning a nullable list of concepts should throw an exception`() {
        Assertions.assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SimpleSchemaDefinitionClassWithNullableListOfConcept::class.java)
        }
    }

}
