package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongClassStructureSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongFunctionSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongConceptQuerySchemaSyntaxException
import org.junit.jupiter.api.Test
import java.util.*

class SchemaApiSchemaQueryTest {

    private interface CommonConceptInterface

    @Concept(facets = [])
    private interface OneConceptClass: CommonConceptInterface

    @Concept(facets = [])
    private interface OtherConceptClass: CommonConceptInterface

    @Concept(facets = [])
    private interface UnsupportedConceptClass: CommonConceptInterface

    @Concept(facets = [])
    private interface ConceptWithoutCommonConceptInterface

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithoutQueryMethods

    @Test
    fun `test schema without query methods should return without exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithoutQueryMethods::class) {
            // do nothing
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private fun interface SchemaWithFunctionalInterface {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class, OtherConceptClass::class])
        fun getMyConcepts(): List<CommonConceptInterface>
    }

    @Test
    fun `test schema with a functional interface SAM with one method should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithFunctionalInterface::class) {
            // do nothing
        }
    }


    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithUnannotatedQueryMethod {
        @Suppress("UNUSED")
        fun getMyConcepts(): List<CommonConceptInterface>
    }

    @Test
    fun `test schema with a unannotated query method should throw an exception`() {
        assertExceptionWithErrorCode(WrongConceptQuerySchemaSyntaxException::class, SchemaErrorCode.MISSING_QUERY_CONCEPT_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithUnannotatedQueryMethod::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithDefaultQueryMethod {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class, OtherConceptClass::class])
        fun getMyConcepts(): List<CommonConceptInterface> {
            return emptyList()
        }
    }

    @Test
    fun `test schema with a query method having a method body should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.FUNCTION_MUST_BE_ABSTRACT) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithDefaultQueryMethod::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodForUnsupportedConceptClass {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OtherConceptClass::class, UnsupportedConceptClass::class])
        fun getMyConcepts(): List<CommonConceptInterface>
    }

    @Test
    fun `test schema with a query method for an unsupported concept class should throw an exception`() {
        assertExceptionWithErrorCode(SyntaxException::class, SchemaErrorCode.INVALID_CONCEPT_TO_QUERY) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodForUnsupportedConceptClass::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodWithEmptyConceptClass {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [])
        fun getMyConcepts(): List<CommonConceptInterface>
    }

    @Test
    fun `test schema with a query method with a empty concept class list should throw an exception`() {
        assertExceptionWithErrorCode(SyntaxException::class, SchemaErrorCode.NO_CONCEPTS_TO_QUERY) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithEmptyConceptClass::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodWithNonConceptClass {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [String::class])
        fun getMyConcepts(): List<CommonConceptInterface>
    }

    @Test
    fun `test schema with a query method with a non-concept class list should throw an exception`() {
        assertExceptionWithErrorCode(SyntaxException::class, SchemaErrorCode.INVALID_CONCEPT_TO_QUERY) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithNonConceptClass::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodsWithValidReturnTypes {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsAsListOfAny(): List<Any>

        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsOfListOfConcreteConceptClass(): List<OneConceptClass>

        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsOfListWithACommonBaseInterface(): List<CommonConceptInterface>

        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsAsSetOfAny(): Set<Any>

        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsOfSetOfConcreteConceptClass(): Set<OneConceptClass>

        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsOfSetWithACommonBaseInterface(): Set<CommonConceptInterface>

        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsAsAny(): Any

        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsConcreteConceptClass(): OneConceptClass

        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsWithACommonBaseInterface(): CommonConceptInterface

        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsAsAnyNullable(): Any?

        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsConcreteConceptClassNullable(): OneConceptClass?

        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsWithACommonBaseInterfaceNullable(): CommonConceptInterface?
    }

    @Test
    fun `test schema with query methods with valid return types should return without exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodsWithValidReturnTypes::class) {
            // do nothing
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodHavingParameter {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConceptsAsListOfAny(myParam: Int): List<Any>
    }

    @Test
    fun `test schema with method having parameters should throw an exception`() {
        assertExceptionWithErrorCode(SyntaxException::class, SchemaErrorCode.FUNCTION_CAN_NOT_HAVE_VALUE_PARAMS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodHavingParameter::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodHavingExtensionFunctionParameter {
        interface MyInterface

        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun MyInterface.getMyConceptsAsListOfAny(myParam: Int): List<Any>
    }

    @Test
    fun `test schema with method having extension function parameter should throw an exception`() {
        assertExceptionWithErrorCode(WrongClassStructureSyntaxException::class, SchemaErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodHavingExtensionFunctionParameter::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithProperty {
        @Suppress("UNUSED")
        val myConcepts: List<CommonConceptInterface>
    }

    @Test
    fun `test schema with property should throw an exception`() {
        assertExceptionWithErrorCode(WrongClassStructureSyntaxException::class, SchemaErrorCode.CLASS_CANNOT_HAVE_PROPERTIES) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithProperty::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryListMethodReturningUnsupportedConcept {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConcepts(): List<OtherConceptClass>
    }

    @Test
    fun `test schema with query method returning wrong concept class list should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryListMethodReturningUnsupportedConcept::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodReturningUnsupportedConcept {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class])
        fun getMyConcept(): OtherConceptClass
    }

    @Test
    fun `test schema with query method returning wrong concept class should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodReturningUnsupportedConcept::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, ConceptWithoutCommonConceptInterface::class ])
    private interface SchemaWithQueryMethodReturningConceptsWithoutCommonInterface {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class, ConceptWithoutCommonConceptInterface::class])
        fun getMyConcepts(): List<CommonConceptInterface>
    }

    @Test
    fun `test schema with query method returning concepts without common interface should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodReturningConceptsWithoutCommonInterface::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, ConceptWithoutCommonConceptInterface::class ])
    private interface SchemaWithQueryMethodReturningConceptWithoutCommonInterface {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class, ConceptWithoutCommonConceptInterface::class])
        fun getMyConcept(): CommonConceptInterface
    }

    @Test
    fun `test schema with query method returning concept without common interface should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodReturningConceptWithoutCommonInterface::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodWithUnsupportedCollectionType {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class, OtherConceptClass::class])
        fun getMyConcepts(): SortedSet<CommonConceptInterface>
    }

    @Test
    fun `test schema with query method with unsupported collection type should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithUnsupportedCollectionType::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodWithoutReturnType {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class, OtherConceptClass::class])
        fun getMyConcepts()
    }

    @Test
    fun `test schema with query method without return type should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.FUNCTION_MUST_HAVE_RETURN_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithoutReturnType::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodWithUnsupportedCollectionValueType {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class, OtherConceptClass::class])
        fun getMyConcepts(): List<String>
    }

    @Test
    fun `test schema with query method with unsupported collection value type should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithUnsupportedCollectionValueType::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodWithUnsupportedValueType {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class, OtherConceptClass::class])
        fun getMyConcepts(): String
    }

    @Test
    fun `test schema with query method with unsupported value type should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithUnsupportedValueType::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodWithNullableCollectionValueType {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class, OtherConceptClass::class])
        fun getMyConcepts(): List<CommonConceptInterface?>
    }

    @Test
    fun `test schema with query method with nullable collection value type should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.RETURN_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithNullableCollectionValueType::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodWithGenericParameterCollectionValueType {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class, OtherConceptClass::class])
        fun <A> getMyConcepts(): List<A>
    }

    @Test
    fun `test schema with query method with generic collection type parameter should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.FUNCTION_HAVE_TYPE_PARAMS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithGenericParameterCollectionValueType::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ OneConceptClass::class, OtherConceptClass::class ])
    private interface SchemaWithQueryMethodWithGenericParameterValueType {
        @Suppress("UNUSED")
        @QueryConcepts(conceptClasses = [OneConceptClass::class, OtherConceptClass::class])
        fun <A> getMyConcepts(): A
    }

    @Test
    fun `test schema with query method with generic type parameter value should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.FUNCTION_HAVE_TYPE_PARAMS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithGenericParameterValueType::class) {
                // do nothing
            }
        }
    }
}