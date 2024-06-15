package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongClassStructureSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongFunctionSyntaxException
import org.junit.jupiter.api.Test
import java.util.*

class SchemaApiConceptQueryTest {

    @Schema(concepts = [ SchemaWithConceptWithoutQueryMethods.ConceptWithoutQueryMethods::class])
    private interface SchemaWithConceptWithoutQueryMethods {
        @Concept(facets = [])
        interface ConceptWithoutQueryMethods
    }

    @Test
    fun `test concept without query method should return without exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithoutQueryMethods::class) {
            // do nothing
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithFunctionalInterface.ConceptWithFunctionalInterface::class ])
    private interface SchemaWithConceptWithFunctionalInterface {
        @Concept(facets = [ConceptWithFunctionalInterface.OneFacet::class])
        fun interface ConceptWithFunctionalInterface {
            @StringFacet
            interface OneFacet

            @Suppress("UNUSED")
            @QueryFacetValue(facetClass = OneFacet::class)
            fun getMyStrings(): List<String>
        }
    }

    @Test
    fun `test schema with a concept with a functional interface SAM with one method should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFunctionalInterface::class) {
            // do nothing
        }
    }


    @Schema(concepts = [ SchemaWithConceptWithUnannotatedQueryMethod.ConceptWithUnannotatedQueryMethod::class ])
    private interface SchemaWithConceptWithUnannotatedQueryMethod {
        @Concept(facets = [ConceptWithUnannotatedQueryMethod.OneFacet::class])
        interface ConceptWithUnannotatedQueryMethod {

            @StringFacet
            interface OneFacet

            @Suppress("Unused")
            fun getFacetValue(): List<Any>
        }
    }

    @Test
    fun `test concept with an unannotated query method should throw an exception`() {
        assertExceptionWithErrorCode(SyntaxException::class, SchemaErrorCode.NO_FACET_TO_QUERY) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithUnannotatedQueryMethod::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithDefaultQueryMethod.ConceptWithDefaultQueryMethod::class ])
    private interface SchemaWithConceptWithDefaultQueryMethod {
        @Concept(facets = [ConceptWithDefaultQueryMethod.OneFacet::class])
        interface ConceptWithDefaultQueryMethod {

            @StringFacet
            interface OneFacet

            @Suppress("Unused")
            @QueryFacetValue(facetClass = OneFacet::class)
            fun getFacetValue(): List<Any> {
                return emptyList()
            }
        }
    }

    @Test
    fun `test concept with an query method having a method body should throw an exception`() {
        assertExceptionWithErrorCode(SyntaxException::class, SchemaErrorCode.FUNCTION_MUST_BE_ABSTRACT) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithDefaultQueryMethod::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithValidAnnotatedQueryMethod.ConceptWithValidAnnotatedQueryMethod::class ])
    private interface SchemaWithConceptWithValidAnnotatedQueryMethod {
        @Concept(facets = [ConceptWithValidAnnotatedQueryMethod.ValidAnnotatedQueryMethodFacet::class])
        interface ConceptWithValidAnnotatedQueryMethod {

            @StringFacet
            interface ValidAnnotatedQueryMethodFacet

            @Suppress("Unused")
            @QueryFacetValue(ValidAnnotatedQueryMethodFacet::class)
            fun getFacetValue(): List<Any>
        }
    }

    @Test
    fun `test concept with a valid annotated method should return without exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithValidAnnotatedQueryMethod::class) {
            // do nothing
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithValidConceptIdAnnotatedQueryMethods.ConceptWithValidConceptIdAnnotatedQueryMethods::class ])
    private interface SchemaWithConceptWithValidConceptIdAnnotatedQueryMethods {
        @Concept(facets = [])
        interface ConceptWithValidConceptIdAnnotatedQueryMethods {

            @Suppress("Unused")
            @QueryConceptIdentifierValue
            fun getConceptId(): ConceptIdentifier
        }
    }

    @Test
    fun `test concept with a valid annotated concept id query method should return without exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithValidConceptIdAnnotatedQueryMethods::class) {
            // do nothing
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithValidStringConceptIdAnnotatedQueryMethods.ConceptWithValidStringConceptIdAnnotatedQueryMethods::class ])
    private interface SchemaWithConceptWithValidStringConceptIdAnnotatedQueryMethods {
        @Concept(facets = [])
        interface ConceptWithValidStringConceptIdAnnotatedQueryMethods {

            @Suppress("Unused")
            @QueryConceptIdentifierValue
            fun getConceptId(): String
        }
    }

    @Test
    fun `test concept with a valid annotated concept id query method returning string should return without exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithValidStringConceptIdAnnotatedQueryMethods::class) {
            // do nothing
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithValidAnyConceptIdAnnotatedQueryMethods.ConceptWithValidAnyConceptIdAnnotatedQueryMethods::class ])
    private interface SchemaWithConceptWithValidAnyConceptIdAnnotatedQueryMethods {
        @Concept(facets = [])
        interface ConceptWithValidAnyConceptIdAnnotatedQueryMethods {

            @Suppress("Unused")
            @QueryConceptIdentifierValue
            fun getConceptId(): Any
        }
    }

    @Test
    fun `test concept with a valid annotated concept id query method returning an any type should return without exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithValidAnyConceptIdAnnotatedQueryMethods::class) {
            // do nothing
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithInvalidCollectionConceptIdAnnotatedQueryMethods.ConceptWithInvalidCollectionConceptIdAnnotatedQueryMethod::class ])
    private interface SchemaWithConceptWithInvalidCollectionConceptIdAnnotatedQueryMethods {
        @Concept(facets = [])
        interface ConceptWithInvalidCollectionConceptIdAnnotatedQueryMethod {

            @Suppress("Unused")
            @QueryConceptIdentifierValue
            fun getConceptId(): List<ConceptIdentifier>
        }
    }

    @Test
    fun `test concept with a invalid annotated concept id query method returning a list collection should return with exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.RETURN_TYPE_COLLECTION_TO_FETCH_CONCEPT_IDENTIFIER_NOT_SUPPORTED) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithInvalidCollectionConceptIdAnnotatedQueryMethods::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithQueryMethodWithoutReturnType.ConceptWithQueryMethodWithoutReturnType::class ])
    private interface SchemaWithConceptWithQueryMethodWithoutReturnType {
        @Concept(facets = [ConceptWithQueryMethodWithoutReturnType.OneFacet::class])
        interface ConceptWithQueryMethodWithoutReturnType {
            @StringFacet
            interface OneFacet

            @Suppress("Unused")
            @QueryFacetValue(OneFacet::class)
            fun getFacetValueWithoutReturnType()
        }
    }

    @Test
    fun `test concept with a query method without return type should return with exception`() {
        assertExceptionWithErrorCode(SyntaxException::class, SchemaErrorCode.FUNCTION_MUST_HAVE_RETURN_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithQueryMethodWithoutReturnType::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithInvalidIntConceptIdAnnotatedQueryMethods.ConceptWithInvalidIntConceptIdAnnotatedQueryMethods::class ])
    private interface SchemaWithConceptWithInvalidIntConceptIdAnnotatedQueryMethods {
        @Concept(facets = [])
        interface ConceptWithInvalidIntConceptIdAnnotatedQueryMethods {

            @Suppress("Unused")
            @QueryConceptIdentifierValue
            fun getConceptId(): Int
        }
    }

    @Test
    fun `test concept with a invalid annotated concept id query method returning an int instead of string should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.RETURN_TYPE_TO_FETCH_CONCEPT_IDENTIFIER_NOT_SUPPORTED) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithInvalidIntConceptIdAnnotatedQueryMethods::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithUnsupportedFacet.ConceptWithUnsupportedFacet::class ])
    private interface SchemaWithConceptWithUnsupportedFacet {
        @Concept(facets = [ConceptWithUnsupportedFacet.OneFacet::class])
        interface ConceptWithUnsupportedFacet {

            @StringFacet
            interface OneFacet

            @StringFacet
            interface UnsupportedFacet

            @Suppress("Unused")
            @QueryFacetValue(UnsupportedFacet::class)
            fun getFacetValue(): List<Any>
        }
    }

    @Test
    fun `test concept with a unsupported facet class should throw an exception`() {
        assertExceptionWithErrorCode(SyntaxException::class, SchemaErrorCode.INVALID_FACET_TO_QUERY) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithUnsupportedFacet::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [
        SchemaWithConceptWithValidFacets.ConceptWithValidFacets::class,
        SchemaWithConceptWithValidFacets.SecondConcept::class,
        SchemaWithConceptWithValidFacets.ThirdConcept::class,
    ])
    private interface SchemaWithConceptWithValidFacets {

        enum class MyEnumeration

        interface CommonConceptInterface

        @Concept(facets = [])
        interface SecondConcept: CommonConceptInterface

        @Concept(facets = [])
        interface ThirdConcept: CommonConceptInterface


        @Concept(facets = [
            ConceptWithValidFacets.TextFacet::class,
            ConceptWithValidFacets.BoolFacet::class,
            ConceptWithValidFacets.NumberFacet::class,
            ConceptWithValidFacets.EnumerationFacet::class,
            ConceptWithValidFacets.SingleConceptReferenceFacet::class,
            ConceptWithValidFacets.MultipleConceptReferenceFacet::class,
        ])
        interface ConceptWithValidFacets {

            @StringFacet
            interface TextFacet

            @IntFacet
            interface NumberFacet

            @BooleanFacet
            interface BoolFacet

            @EnumFacet(enumerationClass = MyEnumeration::class)
            interface EnumerationFacet

            @ReferenceFacet(referencedConcepts = [SecondConcept::class])
            interface SingleConceptReferenceFacet

            @ReferenceFacet(referencedConcepts = [SecondConcept::class, ThirdConcept::class])
            interface MultipleConceptReferenceFacet



            @Suppress("Unused")
            @QueryFacetValue(facetClass = TextFacet::class)
            fun getTextFacetAsListOfAny(): List<Any>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = TextFacet::class)
            fun getTextFacetAsSetOfAny(): Set<Any>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = TextFacet::class)
            fun getTextFacetAsListOfString(): List<String>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = TextFacet::class)
            fun getTextFacetAsSetOfString(): Set<String>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = TextFacet::class)
            fun getTextFacetAsAny(): Any

            @Suppress("Unused")
            @QueryFacetValue(facetClass = TextFacet::class)
            fun getTextFacetAsString(): String

            @Suppress("Unused")
            @QueryFacetValue(facetClass = TextFacet::class)
            fun getTextFacetAsNullableAny(): Any?

            @Suppress("Unused")
            @QueryFacetValue(facetClass = TextFacet::class)
            fun getTextFacetAsNullableString(): String?




            @Suppress("Unused")
            @QueryFacetValue(facetClass = NumberFacet::class)
            fun getNumberFacetAsListOfAny(): List<Any>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = NumberFacet::class)
            fun getNumberFacetAsSetOfAny(): Set<Any>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = NumberFacet::class)
            fun getNumberFacetAsListOfInt(): List<Int>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = NumberFacet::class)
            fun getNumberFacetAsSetOfInt(): Set<Int>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = NumberFacet::class)
            fun getNumberFacetAsAny(): Any

            @Suppress("Unused")
            @QueryFacetValue(facetClass = NumberFacet::class)
            fun getNumberFacetAsInt(): Int

            @Suppress("Unused")
            @QueryFacetValue(facetClass = NumberFacet::class)
            fun getNumberFacetAsNullableAny(): Any?

            @Suppress("Unused")
            @QueryFacetValue(facetClass = NumberFacet::class)
            fun getNumberFacetAsNullableInt(): Int?




            @Suppress("Unused")
            @QueryFacetValue(facetClass = BoolFacet::class)
            fun getBoolFacetAsListOfAny(): List<Any>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = BoolFacet::class)
            fun getBoolFacetAsSetOfAny(): Set<Any>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = BoolFacet::class)
            fun getBoolFacetAsListOfBoolean(): List<Boolean>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = BoolFacet::class)
            fun getBoolFacetAsSetOfBoolean(): Set<Boolean>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = BoolFacet::class)
            fun getBoolFacetAsAny(): Any

            @Suppress("Unused")
            @QueryFacetValue(facetClass = BoolFacet::class)
            fun getBoolFacetAsBoolean(): Boolean

            @Suppress("Unused")
            @QueryFacetValue(facetClass = BoolFacet::class)
            fun getBoolFacetAsNullableAny(): Any?

            @Suppress("Unused")
            @QueryFacetValue(facetClass = BoolFacet::class)
            fun getBoolFacetAsNullableBoolean(): Boolean?




            @Suppress("Unused")
            @QueryFacetValue(facetClass = EnumerationFacet::class)
            fun getEnumerationFacetAsListOfAny(): List<Any>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = EnumerationFacet::class)
            fun getEnumerationFacetAsSetOfAny(): Set<Any>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = EnumerationFacet::class)
            fun getEnumerationFacetAsListOfEnums(): List<MyEnumeration>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = EnumerationFacet::class)
            fun getEnumerationFacetAsSetOfEnums(): Set<MyEnumeration>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = EnumerationFacet::class)
            fun getEnumerationFacetAsListOfStrings(): List<String>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = EnumerationFacet::class)
            fun getEnumerationFacetAsSetOfStrings(): Set<String>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = EnumerationFacet::class)
            fun getEnumerationFacetAsAny(): Any

            @Suppress("Unused")
            @QueryFacetValue(facetClass = EnumerationFacet::class)
            fun getEnumerationFacetAsEnum(): MyEnumeration

            @Suppress("Unused")
            @QueryFacetValue(facetClass = EnumerationFacet::class)
            fun getEnumerationFacetAsString(): String

            @Suppress("Unused")
            @QueryFacetValue(facetClass = EnumerationFacet::class)
            fun getEnumerationFacetAsNullableAny(): Any?

            @Suppress("Unused")
            @QueryFacetValue(facetClass = EnumerationFacet::class)
            fun getEnumerationFacetAsNullableEnum(): MyEnumeration?

            @Suppress("Unused")
            @QueryFacetValue(facetClass = EnumerationFacet::class)
            fun getEnumerationFacetAsNullableString(): String?



            @Suppress("Unused")
            @QueryFacetValue(facetClass = SingleConceptReferenceFacet::class)
            fun getSingleConceptReferenceFacetAsListOfAny(): List<Any>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = SingleConceptReferenceFacet::class)
            fun getSingleConceptReferenceFacetAsSetOfAny(): Set<Any>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = SingleConceptReferenceFacet::class)
            fun getSingleConceptReferenceFacetAsListOfCommonConceptInterface(): List<SecondConcept>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = SingleConceptReferenceFacet::class)
            fun getSingleConceptReferenceFacetAsSetOfCommonConceptInterface(): Set<SecondConcept>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = SingleConceptReferenceFacet::class)
            fun getSingleConceptReferenceFacetAsAny(): Any

            @Suppress("Unused")
            @QueryFacetValue(facetClass = SingleConceptReferenceFacet::class)
            fun getSingleConceptReferenceFacetAsCommonConceptInterface(): SecondConcept

            @Suppress("Unused")
            @QueryFacetValue(facetClass = SingleConceptReferenceFacet::class)
            fun getSingleConceptReferenceFacetAsNullableAny(): Any?

            @Suppress("Unused")
            @QueryFacetValue(facetClass = SingleConceptReferenceFacet::class)
            fun getSingleConceptReferenceFacetAsNullableCommonConceptInterface(): SecondConcept?


            @Suppress("Unused")
            @QueryFacetValue(facetClass = MultipleConceptReferenceFacet::class)
            fun getMultipleConceptReferenceFacetAsListOfAny(): List<Any>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = MultipleConceptReferenceFacet::class)
            fun getMultipleConceptReferenceFacetAsSetOfAny(): Set<Any>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = MultipleConceptReferenceFacet::class)
            fun getMultipleConceptReferenceFacetAsListOfCommonConceptInterface(): List<CommonConceptInterface>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = MultipleConceptReferenceFacet::class)
            fun getMultipleConceptReferenceFacetAsSetOfCommonConceptInterface(): Set<CommonConceptInterface>

            @Suppress("Unused")
            @QueryFacetValue(facetClass = MultipleConceptReferenceFacet::class)
            fun getMultipleConceptReferenceFacetAsAny(): Any

            @Suppress("Unused")
            @QueryFacetValue(facetClass = MultipleConceptReferenceFacet::class)
            fun getMultipleConceptReferenceFacetAsCommonConceptInterface(): CommonConceptInterface

            @Suppress("Unused")
            @QueryFacetValue(facetClass = MultipleConceptReferenceFacet::class)
            fun getMultipleConceptReferenceFacetAsNullableAny(): Any?

            @Suppress("Unused")
            @QueryFacetValue(facetClass = MultipleConceptReferenceFacet::class)
            fun getMultipleConceptReferenceFacetAsNullableCommonConceptInterface(): CommonConceptInterface?
        }
    }

    @Test
    fun `test concept with valid return types should return without exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithValidFacets::class) {
            // do nothing
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithFacetMethodHavingParameter.ConceptWithFacetMethodHavingParameter::class ])
    private interface SchemaWithConceptWithFacetMethodHavingParameter {

        @Concept(facets = [ConceptWithFacetMethodHavingParameter.FacetMethodHavingParameter::class])
        interface ConceptWithFacetMethodHavingParameter {

            @StringFacet
            interface FacetMethodHavingParameter

            @Suppress("Unused")
            @QueryFacetValue(facetClass = FacetMethodHavingParameter::class)
            fun getMyFacetValuesAsListOfAny(myParam: Int): List<Any>
        }
    }

    @Test
    fun `test concept with method having parameters should throw an exception`() {
        assertExceptionWithErrorCode(SyntaxException::class, SchemaErrorCode.FUNCTION_CAN_NOT_HAVE_VALUE_PARAMS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacetMethodHavingParameter::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithFacetMethodHavingExtensionFunctionParameter.ConceptWithFacetMethodHavingExtensionFunctionParameter::class ])
    private interface SchemaWithConceptWithFacetMethodHavingExtensionFunctionParameter {
        interface MyInterface

        @Concept(facets = [ConceptWithFacetMethodHavingExtensionFunctionParameter.FacetMethodHavingExtensionFunctionParameter::class])
        interface ConceptWithFacetMethodHavingExtensionFunctionParameter {

            @StringFacet
            interface FacetMethodHavingExtensionFunctionParameter

            @Suppress("Unused")
            @QueryFacetValue(facetClass = FacetMethodHavingExtensionFunctionParameter::class)
            fun MyInterface.getMyFacetValuesAsListOfAny(): List<Any>
        }
    }

    @Test
    fun `test concept with method having extension function parameter should throw an exception`() {
        assertExceptionWithErrorCode(WrongClassStructureSyntaxException::class, SchemaErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacetMethodHavingExtensionFunctionParameter::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithProperty.ConceptWithProperty::class ])
    private interface SchemaWithConceptWithProperty {

        @Concept(facets = [ConceptWithProperty.OneFacet::class])
        interface ConceptWithProperty {

            @StringFacet
            interface OneFacet

            @Suppress("Unused")
            val myFacetValues: List<Any>
        }
    }

    @Test
    fun `test concept with property should throw an exception`() {
        assertExceptionWithErrorCode(WrongClassStructureSyntaxException::class, SchemaErrorCode.CLASS_CANNOT_HAVE_PROPERTIES) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithProperty::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithQueryMethodReturningWrongFacetTypeList.ConceptWithQueryMethodReturningWrongFacetTypeList::class ])
    private interface SchemaWithConceptWithQueryMethodReturningWrongFacetTypeList {

        @Concept(facets = [ConceptWithQueryMethodReturningWrongFacetTypeList.OneFacet::class])
        interface ConceptWithQueryMethodReturningWrongFacetTypeList {

            @StringFacet
            interface OneFacet

            @Suppress("Unused")
            @QueryFacetValue(facetClass = OneFacet::class)
            fun getMyFacetValuesAsListOfInt(): List<Int>
        }
    }

    @Test
    fun `test concept with method returning wrong facet type list should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.FACET_RETURN_TYPE_NOT_SUPPORTED) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithQueryMethodReturningWrongFacetTypeList::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithQueryMethodReturningWrongFacetType.ConceptWithQueryMethodReturningWrongFacetType::class ])
    private interface SchemaWithConceptWithQueryMethodReturningWrongFacetType {

        @Concept(facets = [ConceptWithQueryMethodReturningWrongFacetType.OneFacet::class])
        interface ConceptWithQueryMethodReturningWrongFacetType {

            @StringFacet
            interface OneFacet

            @Suppress("Unused")
            @QueryFacetValue(facetClass = OneFacet::class)
            fun getMyFacetValuesAsInt(): Int
        }
    }

    @Test
    fun `test concept with method returning wrong facet type should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.FACET_RETURN_TYPE_NOT_SUPPORTED) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithQueryMethodReturningWrongFacetType::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithQueryMethodReturningWrongCollectionType.ConceptWithQueryMethodReturningWrongCollectionType::class ])
    private interface SchemaWithConceptWithQueryMethodReturningWrongCollectionType {

        @Concept(facets = [ConceptWithQueryMethodReturningWrongCollectionType.OneFacet::class])
        interface ConceptWithQueryMethodReturningWrongCollectionType {

            @StringFacet
            interface OneFacet

            @Suppress("Unused")
            @QueryFacetValue(facetClass = OneFacet::class)
            fun getMyFacetValuesAsListOfString(): SortedSet<String>
        }
    }

    @Test
    fun `test concept with method returning wrong facet collection type should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithQueryMethodReturningWrongCollectionType::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithQueryMethodReturningNullableListValue.ConceptWithQueryMethodReturningNullableListValue::class ])
    private interface SchemaWithConceptWithQueryMethodReturningNullableListValue {

        @Concept(facets = [ConceptWithQueryMethodReturningNullableListValue.OneFacet::class])
        interface ConceptWithQueryMethodReturningNullableListValue {

            @StringFacet
            interface OneFacet

            @Suppress("Unused")
            @QueryFacetValue(facetClass = OneFacet::class)
            fun getMyFacetValuesAsListOfString(): List<String?>
        }
    }

    @Test
    fun `test concept with method returning collection with nullable values should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.RETURN_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithQueryMethodReturningNullableListValue::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithQueryMethodReturningGenericParameterListValue.ConceptWithQueryMethodReturningGenericParameterListValue::class ])
    private interface SchemaWithConceptWithQueryMethodReturningGenericParameterListValue {

        @Concept(facets = [ConceptWithQueryMethodReturningGenericParameterListValue.OneFacet::class])
        interface ConceptWithQueryMethodReturningGenericParameterListValue {

            @StringFacet
            interface OneFacet

            @Suppress("Unused")
            @QueryFacetValue(facetClass = OneFacet::class)
            fun <A> getMyFacetValuesAsListOfString(): List<A>
        }
    }

    @Test
    fun `test concept with method returning collection with generic collection type parameter should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.FUNCTION_HAVE_TYPE_PARAMS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithQueryMethodReturningGenericParameterListValue::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [ SchemaWithConceptWithQueryMethodReturningGenericParameterValue.ConceptWithQueryMethodReturningGenericParameterValue::class ])
    private interface SchemaWithConceptWithQueryMethodReturningGenericParameterValue {

        @Concept(facets = [ConceptWithQueryMethodReturningGenericParameterValue.OneFacet::class])
        interface ConceptWithQueryMethodReturningGenericParameterValue {

            @StringFacet
            interface OneFacet

            @Suppress("Unused")
            @QueryFacetValue(facetClass = OneFacet::class)
            fun <A> getMyFacetValueAsGenericParameter(): A
        }
    }

    @Test
    fun `test concept with method returning value with generic type parameter should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.FUNCTION_HAVE_TYPE_PARAMS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithQueryMethodReturningGenericParameterValue::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [
        SchemaWithConceptWithOnlyOneOfTwoConceptQueryMethod.ConceptOne::class,
        SchemaWithConceptWithOnlyOneOfTwoConceptQueryMethod.ConceptTwo::class,
        SchemaWithConceptWithOnlyOneOfTwoConceptQueryMethod.ConceptWithOnlyOneOfTwoConceptQueryMethod::class,
    ])
    private interface SchemaWithConceptWithOnlyOneOfTwoConceptQueryMethod {
        interface CommonConcept

        @Concept(facets = [])
        interface ConceptOne: CommonConcept

        @Concept(facets = [])
        interface ConceptTwo: CommonConcept

        @Concept(facets = [ConceptWithOnlyOneOfTwoConceptQueryMethod.MyReferenceFacet::class])
        interface ConceptWithOnlyOneOfTwoConceptQueryMethod {

            @ReferenceFacet(referencedConcepts = [ConceptOne::class, ConceptTwo::class])
            interface MyReferenceFacet

            @Suppress("Unused")
            @QueryFacetValue(facetClass = MyReferenceFacet::class)
            fun getReferencedConcepts(): List<ConceptOne>
        }
    }

    @Test
    fun `test concept with query method fetching only one of multiple allowed concepts should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.FACET_RETURN_TYPE_NOT_SUPPORTED) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithOnlyOneOfTwoConceptQueryMethod::class) {
                // do nothing
            }
        }
    }

    @Schema(concepts = [
        SchemaWithConceptWithUnreferencedConceptQueryMethod.ReferencedConcept::class,
        SchemaWithConceptWithUnreferencedConceptQueryMethod.NotReferencedConcept::class,
        SchemaWithConceptWithUnreferencedConceptQueryMethod.ConceptWithUnreferencedConceptQueryMethod::class,
    ])
    private interface SchemaWithConceptWithUnreferencedConceptQueryMethod {
        interface CommonConcept

        @Concept(facets = [])
        interface ReferencedConcept: CommonConcept

        @Concept(facets = [])
        interface NotReferencedConcept: CommonConcept

        @Concept(facets = [ConceptWithUnreferencedConceptQueryMethod.MyReferenceFacet::class])
        interface ConceptWithUnreferencedConceptQueryMethod {

            @ReferenceFacet(referencedConcepts = [ReferencedConcept::class])
            interface MyReferenceFacet

            @Suppress("Unused")
            @QueryFacetValue(facetClass = MyReferenceFacet::class)
            fun getReferencedConcepts(): List<NotReferencedConcept>
        }
    }

    @Test
    fun `test concept with query method fetching an not referenced concept should throw an exception`() {
        assertExceptionWithErrorCode(WrongFunctionSyntaxException::class, SchemaErrorCode.FACET_RETURN_TYPE_NOT_SUPPORTED) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithUnreferencedConceptQueryMethod::class) {
                // do nothing
            }
        }
    }

}