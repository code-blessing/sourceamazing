package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MalformedSchemaException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SchemaQueryValidatorTest {

    private interface CommonConceptInterface

    @Concept(facets = [])
    private interface OneConceptClass: CommonConceptInterface

    @Concept(facets = [])
    private interface OtherConceptClass: CommonConceptInterface

    @Concept(facets = [])
    private interface UnsupportedConceptClass

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithoutAccessorMethods

    @Test
    fun `test schema without accessor method should return without exception`() {
        SchemaQueryValidator.validateAccessorMethodsOfSchemaDefinitionClass(SchemaWithoutAccessorMethods::class)
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithUnannotatedAccessorMethods {
        fun getMyConcepts(): List<Any>
    }

    @Test
    fun `test schema with a unannotated method should throw an exception`() {
        assertThrows(MalformedSchemaException::class.java) {
            SchemaQueryValidator.validateAccessorMethodsOfSchemaDefinitionClass(SchemaWithUnannotatedAccessorMethods::class)
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithUnsupportedConceptClass {
        @QueryConcepts(conceptClasses = [OtherConceptClass::class, UnsupportedConceptClass::class])
        fun getMyConcepts(): List<Any>
    }

    @Test
    fun `test schema with a unsupported concept class should throw an exception`() {
        assertThrows(MalformedSchemaException::class.java) {
            SchemaQueryValidator.validateAccessorMethodsOfSchemaDefinitionClass(SchemaWithUnsupportedConceptClass::class)
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithEmptyConceptClass {
        @QueryConcepts(conceptClasses = [])
        fun getMyConcepts(): List<Any>
    }

    @Test
    fun `test schema with a empty concept class list should throw an exception`() {
        assertThrows(MalformedSchemaException::class.java) {
            SchemaQueryValidator.validateAccessorMethodsOfSchemaDefinitionClass(SchemaWithEmptyConceptClass::class)
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithValidReturnTypes {
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsAsListOfAny(): List<Any>

        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsOfListOfConcreteConceptClass(): List<OneConceptClass>

        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsOfListWithACommonBaseInterface(): List<CommonConceptInterface>

        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsAsSetOfAny(): Set<Any>

        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsOfSetOfConcreteConceptClass(): Set<OneConceptClass>

        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsOfSetWithACommonBaseInterface(): Set<CommonConceptInterface>

        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsAsAny(): Any

        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsConcreteConceptClass(): OneConceptClass

        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsWithACommonBaseInterface(): CommonConceptInterface

        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsAsAnyNullable(): Any?

        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsConcreteConceptClassNullable(): OneConceptClass?

        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsWithACommonBaseInterfaceNullable(): CommonConceptInterface?
    }

    @Test
    fun `test schema with valid return types should return without exception`() {
        SchemaQueryValidator.validateAccessorMethodsOfSchemaDefinitionClass(SchemaWithValidReturnTypes::class)
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithMethodHavingParameter {
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsAsListOfAny(myParam: Int): List<Any>
    }

    @Test
    fun `test schema with method having parameters should throw an exception`() {
        assertThrows(MalformedSchemaException::class.java) {
            SchemaQueryValidator.validateAccessorMethodsOfSchemaDefinitionClass(SchemaWithMethodHavingParameter::class)
        }
    }

}