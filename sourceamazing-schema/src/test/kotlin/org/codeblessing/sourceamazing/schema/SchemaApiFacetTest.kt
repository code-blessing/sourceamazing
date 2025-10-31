package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.exceptions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.*

class SchemaApiFacetTest {
    private interface ReferenceConcept

    private interface ConceptWithTextFacet {

        @Suppress("UNUSED")
        @Facet
        val name: String
    }

    @Test
    fun `test create an schema with concept class having a text facet should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = ConceptWithTextFacet::class) { schemaContext ->
            withRootInstance<ConceptWithTextFacet>(schemaContext) {
                // do nothing
            }
        }
    }

    private interface DefinitionClassWithValidEnumFacet {
        enum class MyValidEnum

        @Suppress("UNUSED")
        @Facet
        val myEnum: MyValidEnum
    }

    @Test
    fun `test concept having an enumeration facet with a valid enumeration type should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithValidEnumFacet::class) { schemaContext ->
            withRootInstance<DefinitionClassWithValidEnumFacet>(schemaContext) {
                // do nothing
            }
        }
    }

    private enum class MyPrivateEnum {
        @Suppress("UNUSED") A,
        @Suppress("UNUSED") B,
        @Suppress("UNUSED") C,

    }

    private interface SchemaWithConceptWithPrivateEnumFacet {
        @Suppress("UNUSED")
        @Facet
        val myEnum: MyPrivateEnum
    }

    @Test
    fun `test create an schema with concept class having a enum facet that has modifier private should throw an exception`() {
        assertExceptionWithErrorCode(WrongTypeSyntaxException::class, SchemaErrorCode.FACET_ENUM_HAS_PRIVATE_MODIFIER) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithPrivateEnumFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithPrivateEnumFacet>(schemaContext) {
                    // do nothing
                }
            }
        }
    }


    private interface DefinitionClassWithUnannotatedFacet {
        @Suppress("UNUSED")
        val unannotatedProperty: String
    }

    @Test
    fun `test create a concept with an unannotated facet should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.MISSING_FACET_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithUnannotatedFacet::class) { schemaContext ->
                withRootInstance<DefinitionClassWithUnannotatedFacet>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithFacetHavingMembers {

        @Suppress("UNUSED")
        val name: String

        @Suppress("UNUSED")
        fun oneMemberOnFacetInterface()
    }

    @Test
    fun `test create a concept with an facet having members on it should throw an exception`() {
        assertExceptionWithErrorCode(WrongClassStructureSyntaxException::class, SchemaErrorCode.CLASS_CANNOT_HAVE_MEMBER_FUNCTIONS) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithFacetHavingMembers::class) { schemaContext ->
                withRootInstance<DefinitionClassWithFacetHavingMembers>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithFacetObjectInsteadOfInterface {
        object FacetObjectInsteadOfInterface

        @Suppress("UNUSED")
        @Facet
        val myProperty: FacetObjectInsteadOfInterface
    }

    @Test
    fun `test create a concept with an facet object instead of interface should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.PROPERTY_RETURN_TYPE_MUST_BE_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithFacetObjectInsteadOfInterface::class) { schemaContext ->
                withRootInstance<DefinitionClassWithFacetObjectInsteadOfInterface>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithFacetAnnotationInterfaceInsteadOfInterface {
        annotation class FacetAnnotationInterfaceInsteadOfInterface

        @Suppress("UNUSED")
        @Facet
        val myProperty: FacetAnnotationInterfaceInsteadOfInterface
    }

    @Test
    fun `test create a concept with an facet annotation interface instead of interface should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.PROPERTY_RETURN_TYPE_MUST_BE_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithFacetAnnotationInterfaceInsteadOfInterface::class) { schemaContext ->
                withRootInstance<DefinitionClassWithFacetAnnotationInterfaceInsteadOfInterface>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithUnitTypeOnFacet {
        @Suppress("UNUSED")
        @Facet
        val myProperty: Unit
    }

    @Test
    fun `test facet with unit type should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.PROPERTY_MUST_HAVE_RETURN_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithUnitTypeOnFacet::class) { schemaContext ->
                withRootInstance<DefinitionClassWithUnitTypeOnFacet>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithEmptyReferenceFacet {
        @Suppress("UNUSED")
        @Facet
        @References([])
        val myProperty: List<ReferenceConcept>
    }

    @Test
    fun `test concept having an empty reference facet should throw an exception`() {
        assertExceptionWithErrorCode(WrongTypeSyntaxException::class, SchemaErrorCode.FACET_REFERENCE_EMPTY_CONCEPT_LIST) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithEmptyReferenceFacet::class) { schemaContext ->
                withRootInstance<DefinitionClassWithEmptyReferenceFacet>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithAbstractReferencedConceptFacet {
        abstract class AbstractReferenceConcept
        @Suppress("UNUSED")
        @Facet
        @References([AbstractReferenceConcept::class])
        val myProperty: List<AbstractReferenceConcept>
    }

    @Test
    fun `test reference facet to unknown concept should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.PROPERTY_RETURN_TYPE_MUST_BE_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithAbstractReferencedConceptFacet::class) { schemaContext ->
                withRootInstance<DefinitionClassWithAbstractReferencedConceptFacet>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithSelfReference {
        abstract class AbstractReferenceConcept
        @Suppress("UNUSED")
        @Facet
        @References([DefinitionClassWithSelfReference::class])
        val myProperty: List<DefinitionClassWithSelfReference>
    }

    @Test
    @Disabled("Maybe this is possible. Have to investigate")
    fun `test reference facet to a the self class should throw an exception`() {
        assertExceptionWithErrorCode(WrongTypeSyntaxException::class, SchemaErrorCode.FACET_UNKNOWN_REFERENCED_CONCEPT) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithSelfReference::class) { schemaContext ->
                withRootInstance<DefinitionClassWithSelfReference>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithFacetWithFunctionType {
        @Suppress("UNUSED")
        @Facet
        val myProperty: () -> Unit
    }

    @Test
    fun `test facet with type parameter should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithFacetWithFunctionType::class) { schemaContext ->
                withRootInstance<DefinitionClassWithFacetWithFunctionType>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private fun interface SchemaWithConceptWithFunctionalInterface {
        @Suppress("UNUSED")
        fun getMyStrings(): List<String>
    }

    @Test
    fun `test schema with a concept with a functional interface SAM with one method should fail`() {
        assertExceptionWithErrorCode(WrongClassStructureSyntaxException::class, SchemaErrorCode.CLASS_CANNOT_HAVE_MEMBER_FUNCTIONS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFunctionalInterface::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFunctionalInterface>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionInterfaceWithQueryMethodReturningGenericParameterListValue {

        @Suppress("Unused")
        fun <A> getMyFacetValuesAsListOfString(): List<A>
    }

    @Test
    fun `test concept with method returning collection with generic collection type parameter should throw an exception`() {
        assertExceptionWithErrorCode(WrongClassStructureSyntaxException::class, SchemaErrorCode.CLASS_CANNOT_HAVE_MEMBER_FUNCTIONS) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionInterfaceWithQueryMethodReturningGenericParameterListValue::class) { schemaContext ->
                withRootInstance<DefinitionInterfaceWithQueryMethodReturningGenericParameterListValue>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithUnannotatedFacet {
        @Suppress("Unused")
        val myProperty: List<Any>
    }

    @Test
    fun `test concept with an unannotated query method should throw an exception`() {
        assertExceptionWithErrorCode(SyntaxException::class, SchemaErrorCode.MISSING_FACET_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithUnannotatedFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithUnannotatedFacet>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithGetterFacet {
        @Suppress("Unused")
        @Facet
        val myProperty: List<Any>
            get() = emptyList()
    }

    @Test
    fun `test concept with an query method having a method body should throw an exception`() {
        assertExceptionWithErrorCode(SyntaxException::class, SchemaErrorCode.FUNCTION_MUST_BE_ABSTRACT) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithGetterFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithGetterFacet>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithExtensionProperty {

        @Suppress("Unused")
        @Facet
        val Int.myProperty: String
    }

    @Test
    fun `test concept with extension property should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.PROPERTY_MUST_NOT_HAVE_EXTENSION_TYPE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithExtensionProperty::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithExtensionProperty>(schemaContext) {
                    // do nothing
                }
            }
        }
    }


    private interface SchemaWithConceptWithQueryMethodReturningWrongCollectionType {

        @Suppress("Unused")
        @Facet
        val myProperty: SortedSet<String>
    }

    @Test
    fun `test concept with method returning wrong facet collection type should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithQueryMethodReturningWrongCollectionType::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithQueryMethodReturningWrongCollectionType>(schemaContext) {
                    // do nothing
                }
            }
        }
    }


    private interface SchemaWithConceptWithQueryMethodReturningNullableListValue {

        @Suppress("Unused")
        @Facet
        val myProperty: List<String?>
    }

    @Test
    fun `test concept with method returning collection with nullable values should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.RETURN_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithQueryMethodReturningNullableListValue::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithQueryMethodReturningNullableListValue>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithReferencesAnnotationButNotReferenceType {
        interface MyConcept

        @Suppress("Unused")
        @Facet
        @References([MyConcept::class])
        val referencedConcepts: List<String>
    }

    @Test
    fun `test concept with query method with references annotation but not a reference type should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.REFERENCE_ANNOTATION_ONLY_FOR_REFERENCE_TYPES) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithReferencesAnnotationButNotReferenceType::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithReferencesAnnotationButNotReferenceType>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithOnlyOneOfTwoConceptQueryMethod {
        interface CommonConcept
        interface ConceptOne: CommonConcept
        interface ConceptTwo: CommonConcept

        @Suppress("Unused")
        @Facet
        @References([ConceptOne::class, ConceptTwo::class])
        val referencedConcepts: List<ConceptOne>
    }

    @Test
    fun `test concept with query method fetching only one of multiple allowed concepts should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithOnlyOneOfTwoConceptQueryMethod::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithOnlyOneOfTwoConceptQueryMethod>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithUnreferencedConceptQueryMethod {
        interface CommonConcept
        interface ReferencedConcept: CommonConcept
        interface NotReferencedConcept: CommonConcept

        @Suppress("Unused")
        @References([ReferencedConcept::class])
        val referencedConcepts: List<NotReferencedConcept>
    }

    @Test
    fun `test concept with query method fetching an not referenced concept should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.MISSING_FACET_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithUnreferencedConceptQueryMethod::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithUnreferencedConceptQueryMethod>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithQueryMethodReturningConceptsWithoutCommonInterface {
        interface CommonConcept
        interface ConceptWithCommonConceptInterface: CommonConcept
        interface ConceptWithoutCommonConceptInterface

        @Suppress("UNUSED")
        @Facet
        @References([ConceptWithCommonConceptInterface::class, ConceptWithoutCommonConceptInterface::class])
        val myReferences: List<CommonConcept>
    }

    @Test
    fun `test schema with query method returning concepts without common interface should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodReturningConceptsWithoutCommonInterface::class) { schemaContext ->
                withRootInstance<SchemaWithQueryMethodReturningConceptsWithoutCommonInterface>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithQueryMethodWithUnsupportedCollectionType {
        interface CommonConcept
        interface ConceptOne: CommonConcept
        interface ConceptTwo: CommonConcept

        @Suppress("Unused")
        @Facet
        @References([ConceptOne::class, ConceptTwo::class])
        val referencedConcepts: SortedSet<CommonConcept>
    }

    @Test
    fun `test schema with query method with unsupported collection type should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithUnsupportedCollectionType::class) { schemaContext ->
                withRootInstance<SchemaWithQueryMethodWithUnsupportedCollectionType>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithQueryMethodWithUnsupportedCollectionValueType {
        interface CommonConcept
        interface ConceptOne: CommonConcept
        interface ConceptTwo: CommonConcept
        interface ConceptNotCommon

        @Suppress("Unused")
        @Facet
        @References([ConceptOne::class, ConceptTwo::class])
        val referencedConcepts: List<ConceptNotCommon>
    }

    @Test
    fun `test schema with query method with unsupported collection value type should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithUnsupportedCollectionValueType::class) { schemaContext ->
                withRootInstance<SchemaWithQueryMethodWithUnsupportedCollectionValueType>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithQueryMethodWithNullableCollectionType {
        interface CommonConcept
        interface ConceptOne: CommonConcept
        interface ConceptTwo: CommonConcept

        @Suppress("Unused")
        @Facet
        @References([ConceptOne::class, ConceptTwo::class])
        val referencedConcepts: List<CommonConcept?>
    }

    @Test
    fun `test schema with query method with nullable collection type should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithUnsupportedCollectionType::class) { schemaContext ->
                withRootInstance<SchemaWithQueryMethodWithUnsupportedCollectionType>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithQueryMethodWithUnsupportedValueType {
        interface CommonConcept
        interface ConceptOne: CommonConcept
        interface ConceptTwo: CommonConcept
        interface ConceptNotCommon


        @Suppress("UNUSED")
        @Facet
        @References([ConceptOne::class, ConceptTwo::class])
        val myConcepts: ConceptNotCommon
    }

    @Test
    fun `test schema with query method with unsupported value type should throw an exception`() {
        assertExceptionWithErrorCode(WrongPropertySyntaxException::class, SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithUnsupportedValueType::class) { schemaContext ->
                withRootInstance<SchemaWithQueryMethodWithUnsupportedValueType>(schemaContext) {
                    // do nothing
                }
            }
        }
    }


    private interface SchemaWithConceptWithValidFacets {

        enum class MyEnumeration

        interface CommonConcept

        interface SpecificConceptOne: CommonConcept
        interface SpecificConceptTwo: CommonConcept


        @Suppress("Unused")
        @Facet
        val myTextFacetAsListOfString: List<String>

        @Suppress("Unused")
        @Facet
        val myTextFacetAsSetOfString: Set<String>

        @Suppress("Unused")
        @Facet
        val myTextFacetAsString: String

        @Suppress("Unused")
        @Facet
        val myTextFacetAsNullableString: String?




        @Suppress("Unused")
        @Facet
        val myNumberFacetAsListOfInt: List<Int>

        @Suppress("Unused")
        @Facet
        val myNumberFacetAsSetOfInt: Set<Int>

        @Suppress("Unused")
        @Facet
        val myNumberFacetAsInt: Int

        @Suppress("Unused")
        @Facet
        val myNumberFacetAsNullableInt: Int?




        @Suppress("Unused")
        @Facet
        val myBoolFacetAsListOfBoolean: List<Boolean>

        @Suppress("Unused")
        @Facet
        val myBoolFacetAsSetOfBoolean: Set<Boolean>

        @Suppress("Unused")
        @Facet
        val myBoolFacetAsBoolean: Boolean

        @Suppress("Unused")
        @Facet
        val myBoolFacetAsNullableBoolean: Boolean?




        @Suppress("Unused")
        @Facet
        val myEnumerationFacetAsListOfEnums: List<MyEnumeration>

        @Suppress("Unused")
        @Facet
        val myEnumerationFacetAsSetOfEnums: Set<MyEnumeration>

        @Suppress("Unused")
        @Facet
        val myEnumerationFacetAsEnum: MyEnumeration

        @Suppress("Unused")
        @Facet
        val myEnumerationFacetAsNullableEnum: MyEnumeration?



        @Suppress("Unused")
        @Facet
        val mySingleConceptReferenceFacetAsListOfCommonConceptInterface: List<SpecificConceptOne>

        @Suppress("Unused")
        @Facet
        val mySingleConceptReferenceFacetAsSetOfCommonConceptInterface: Set<SpecificConceptOne>

        @Suppress("Unused")
        @Facet
        val mySingleConceptReferenceFacetAsCommonConceptInterface: SpecificConceptOne

        @Suppress("Unused")
        @Facet
        val mySingleConceptReferenceFacetAsNullableCommonConceptInterface: SpecificConceptOne?


        @Suppress("Unused")
        @Facet
        @References([SpecificConceptOne::class, SpecificConceptTwo::class])
        val myMultipleConceptReferenceFacetAsListOfCommonConcept: List<CommonConcept>

        @Suppress("Unused")
        @Facet
        @References([SpecificConceptOne::class, SpecificConceptTwo::class])
        val myMultipleConceptReferenceFacetAsSetOfCommonConcept: Set<CommonConcept>

        @Suppress("Unused")
        @Facet
        @References([SpecificConceptOne::class, SpecificConceptTwo::class])
        val myMultipleConceptReferenceFacetAsCommonConcept: CommonConcept

        @Suppress("Unused")
        @Facet
        @References([SpecificConceptOne::class, SpecificConceptTwo::class])
        val myMultipleConceptReferenceFacetAsNullableCommonConcept: CommonConcept?
    }

    @Test
    fun `test concept with valid return types should return without exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithValidFacets::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithValidFacets>(schemaContext) {
                // do nothing
            }
        }
    }
}
