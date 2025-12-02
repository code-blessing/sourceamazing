package org.codeblessing.sourceamazing.schema

import java.util.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.WrongClassStructureSyntaxException
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.WrongFacetSchemaException
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.WrongPropertySyntaxException
import org.codeblessing.sourceamazing.schema.api.toConceptName
import org.codeblessing.sourceamazing.schema.api.toFacetName
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Suppress("UNUSED", "Unused")
class SchemaApiFacetTest {
    private interface ReferenceConcept

    private interface ConceptWithTextFacet {

        val name: String?
    }

    @Test
    fun `test create an schema with concept class having a text facet should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = ConceptWithTextFacet::class) { schemaContext ->
            schemaContext.withDefaultValueRootInstance<ConceptWithTextFacet> {
                // do nothing
            }
        }
    }

    private interface DefinitionClassWithValidEnumFacet {
        enum class MyValidEnum

        val myEnum: MyValidEnum?
    }

    @Test
    fun `test concept having an enumeration facet with a valid enumeration type should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithValidEnumFacet::class) { schemaContext ->
            schemaContext.withDefaultValueRootInstance<DefinitionClassWithValidEnumFacet> {
                // do nothing
            }
        }
    }

    private enum class MyPrivateEnum {
        A,
        B,
        C,
    }

    private interface SchemaWithConceptWithPrivateEnumFacet {
        val myEnum: MyPrivateEnum
    }

    @Test
    fun `test create an schema with concept class having a enum facet that has modifier private should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongFacetSchemaException::class,
            SchemaErrorCode.FACET_ENUM_HAS_PRIVATE_MODIFIER,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithPrivateEnumFacet::class) { schemaContext
                ->
                schemaContext.withDefaultValueRootInstance<SchemaWithConceptWithPrivateEnumFacet> {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithFacetHavingMembers {

        val name: String

        fun oneMemberOnFacetInterface()
    }

    @Test
    fun `test create a concept with an facet having members on it should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongClassStructureSyntaxException::class,
            SchemaErrorCode.CLASS_CANNOT_HAVE_MEMBER_FUNCTIONS,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithFacetHavingMembers::class) { schemaContext
                ->
                schemaContext.withDefaultValueRootInstance<DefinitionClassWithFacetHavingMembers> {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithFacetObjectInsteadOfInterface {
        object FacetObjectInsteadOfInterface

        val myProperty: FacetObjectInsteadOfInterface
    }

    @Test
    fun `test create a concept with an facet object instead of interface should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.PROPERTY_RETURN_TYPE_MUST_BE_INTERFACE,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithFacetObjectInsteadOfInterface::class) {
                schemaContext ->
                schemaContext.withDefaultValueRootInstance<DefinitionClassWithFacetObjectInsteadOfInterface> {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithFacetAnnotationInterfaceInsteadOfInterface {
        annotation class FacetAnnotationInterfaceInsteadOfInterface

        val myProperty: FacetAnnotationInterfaceInsteadOfInterface
    }

    @Test
    fun `test create a concept with an facet annotation interface instead of interface should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.PROPERTY_RETURN_TYPE_MUST_BE_INTERFACE,
        ) {
            SchemaApi.withSchema(
                schemaDefinitionClass = DefinitionClassWithFacetAnnotationInterfaceInsteadOfInterface::class
            ) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<
                    DefinitionClassWithFacetAnnotationInterfaceInsteadOfInterface
                > {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithUnitTypeOnFacet {
        val myProperty: Unit
    }

    @Test
    fun `test facet with unit type should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.PROPERTY_MUST_HAVE_RETURN_TYPE,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithUnitTypeOnFacet::class) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<DefinitionClassWithUnitTypeOnFacet> {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithEmptyReferenceFacet {
        @References([]) val myProperty: List<ReferenceConcept>
    }

    @Test
    fun `test concept having an empty reference facet should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.FACET_REFERENCE_EMPTY_CONCEPT_LIST,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithEmptyReferenceFacet::class) { schemaContext
                ->
                schemaContext.withDefaultValueRootInstance<DefinitionClassWithEmptyReferenceFacet> {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithDuplicateReferenceFacet {
        @References([ReferenceConcept::class, ReferenceConcept::class]) val myProperty: List<ReferenceConcept>
    }

    @Test
    fun `test concept having duplicate references should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.REFERENCE_ANNOTATION_CONTAINS_DUPLICATES,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithDuplicateReferenceFacet::class) {
                schemaContext ->
                schemaContext.withDefaultValueRootInstance<DefinitionClassWithDuplicateReferenceFacet> {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithAbstractReferencedConceptFacet {
        abstract class AbstractReferenceConcept

        @References([AbstractReferenceConcept::class]) val myProperty: List<AbstractReferenceConcept>
    }

    @Test
    fun `test reference facet to unknown concept should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.PROPERTY_RETURN_TYPE_MUST_BE_INTERFACE,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithAbstractReferencedConceptFacet::class) {
                schemaContext ->
                schemaContext.withDefaultValueRootInstance<DefinitionClassWithAbstractReferencedConceptFacet> {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithSelfReference {
        abstract class AbstractReferenceConcept

        @References([DefinitionClassWithSelfReference::class]) val myProperty: List<DefinitionClassWithSelfReference>
    }

    @Test
    @Disabled("Maybe this is possible. Have to investigate")
    fun `test reference facet to a the self class should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongFacetSchemaException::class,
            SchemaErrorCode.FACET_UNKNOWN_REFERENCED_CONCEPT,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithSelfReference::class) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<DefinitionClassWithSelfReference> {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionClassWithFacetWithFunctionType {
        val myProperty: () -> Unit
    }

    @Test
    fun `test facet with type parameter should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = DefinitionClassWithFacetWithFunctionType::class) {
                schemaContext ->
                schemaContext.withDefaultValueRootInstance<DefinitionClassWithFacetWithFunctionType> {
                    // do nothing
                }
            }
        }
    }

    private fun interface SchemaWithConceptWithFunctionalInterface {
        fun getMyStrings(): List<String>
    }

    @Test
    fun `test schema with a concept with a functional interface SAM with one method should fail`() {
        assertExceptionWithErrorCode(
            WrongClassStructureSyntaxException::class,
            SchemaErrorCode.CLASS_CANNOT_HAVE_MEMBER_FUNCTIONS,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFunctionalInterface::class) {
                schemaContext ->
                schemaContext.withDefaultValueRootInstance<SchemaWithConceptWithFunctionalInterface> {
                    // do nothing
                }
            }
        }
    }

    private interface DefinitionInterfaceWithQueryMethodReturningGenericParameterListValue {

        fun <A> getMyFacetValuesAsListOfString(): List<A>
    }

    @Test
    fun `test concept with method returning collection with generic collection type parameter should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongClassStructureSyntaxException::class,
            SchemaErrorCode.CLASS_CANNOT_HAVE_MEMBER_FUNCTIONS,
        ) {
            SchemaApi.withSchema(
                schemaDefinitionClass = DefinitionInterfaceWithQueryMethodReturningGenericParameterListValue::class
            ) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<
                    DefinitionInterfaceWithQueryMethodReturningGenericParameterListValue
                > {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithGetterFacet {
        val myProperty: List<Any>
            get() = emptyList()
    }

    @Test
    fun `test concept with an query method having a method body should throw an exception`() {
        assertExceptionWithErrorCode(SyntaxException::class, SchemaErrorCode.FUNCTION_MUST_BE_ABSTRACT) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithGetterFacet::class) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<SchemaWithConceptWithGetterFacet> {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithExtensionProperty {

        val Int.myProperty: String
    }

    @Test
    fun `test concept with extension property should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.PROPERTY_MUST_NOT_HAVE_EXTENSION_TYPE,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithExtensionProperty::class) { schemaContext
                ->
                schemaContext.withDefaultValueRootInstance<SchemaWithConceptWithExtensionProperty> {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithQueryMethodReturningWrongCollectionType {

        val myProperty: SortedSet<String>
    }

    @Test
    fun `test concept with method returning wrong facet collection type should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS,
        ) {
            SchemaApi.withSchema(
                schemaDefinitionClass = SchemaWithConceptWithQueryMethodReturningWrongCollectionType::class
            ) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<
                    SchemaWithConceptWithQueryMethodReturningWrongCollectionType
                > {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithQueryMethodReturningNullableListValue {

        val myProperty: List<String?>
    }

    @Test
    fun `test concept with method returning collection with nullable values should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.RETURN_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED,
        ) {
            SchemaApi.withSchema(
                schemaDefinitionClass = SchemaWithConceptWithQueryMethodReturningNullableListValue::class
            ) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<SchemaWithConceptWithQueryMethodReturningNullableListValue> {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithReferencesAnnotationButNotReferenceType {
        interface MyConcept

        @References([MyConcept::class]) val referencedConcepts: List<String>
    }

    @Test
    fun `test concept with query method with references annotation but not a reference type should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.REFERENCE_ANNOTATION_ONLY_FOR_REFERENCE_TYPES,
        ) {
            SchemaApi.withSchema(
                schemaDefinitionClass = SchemaWithConceptWithReferencesAnnotationButNotReferenceType::class
            ) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<
                    SchemaWithConceptWithReferencesAnnotationButNotReferenceType
                > {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithOnlyOneOfTwoConceptQueryMethod {
        interface CommonConcept

        interface ConceptOne : CommonConcept

        interface ConceptTwo : CommonConcept

        @References([ConceptOne::class, ConceptTwo::class]) val referencedConcepts: List<ConceptOne>
    }

    @Test
    fun `test concept with query method fetching only one of multiple allowed concepts should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithOnlyOneOfTwoConceptQueryMethod::class) {
                schemaContext ->
                schemaContext.withDefaultValueRootInstance<SchemaWithConceptWithOnlyOneOfTwoConceptQueryMethod> {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithUnreferencedConceptQueryMethod {
        interface CommonConcept

        interface ReferencedConcept : CommonConcept

        interface NotReferencedConcept : CommonConcept

        @References([ReferencedConcept::class]) val referencedConcepts: List<NotReferencedConcept>
    }

    @Test
    fun `test concept with query method fetching an not referenced concept should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithUnreferencedConceptQueryMethod::class) {
                schemaContext ->
                schemaContext.withDefaultValueRootInstance<SchemaWithConceptWithUnreferencedConceptQueryMethod> {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithQueryMethodReturningConceptsWithoutCommonInterface {
        interface CommonConcept

        interface ConceptWithCommonConceptInterface : CommonConcept

        interface ConceptWithoutCommonConceptInterface

        @References([ConceptWithCommonConceptInterface::class, ConceptWithoutCommonConceptInterface::class])
        val myReferences: List<CommonConcept>
    }

    @Test
    fun `test schema with query method returning concepts without common interface should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE,
        ) {
            SchemaApi.withSchema(
                schemaDefinitionClass = SchemaWithQueryMethodReturningConceptsWithoutCommonInterface::class
            ) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<
                    SchemaWithQueryMethodReturningConceptsWithoutCommonInterface
                > {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithQueryMethodWithUnsupportedCollectionType {
        interface CommonConcept

        interface ConceptOne : CommonConcept

        interface ConceptTwo : CommonConcept

        @References([ConceptOne::class, ConceptTwo::class]) val referencedConcepts: SortedSet<CommonConcept>
    }

    @Test
    fun `test schema with query method with unsupported collection type should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithUnsupportedCollectionType::class) {
                schemaContext ->
                schemaContext.withDefaultValueRootInstance<SchemaWithQueryMethodWithUnsupportedCollectionType> {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithQueryMethodWithUnsupportedCollectionValueType {
        interface CommonConcept

        interface ConceptOne : CommonConcept

        interface ConceptTwo : CommonConcept

        interface ConceptNotCommon

        @References([ConceptOne::class, ConceptTwo::class]) val referencedConcepts: List<ConceptNotCommon>
    }

    @Test
    fun `test schema with query method with unsupported collection value type should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE,
        ) {
            SchemaApi.withSchema(
                schemaDefinitionClass = SchemaWithQueryMethodWithUnsupportedCollectionValueType::class
            ) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<SchemaWithQueryMethodWithUnsupportedCollectionValueType> {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithQueryMethodWithNullableCollectionType {
        interface CommonConcept

        interface ConceptOne : CommonConcept

        interface ConceptTwo : CommonConcept

        @References([ConceptOne::class, ConceptTwo::class]) val referencedConcepts: List<CommonConcept?>
    }

    @Test
    fun `test schema with query method with nullable collection type should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithUnsupportedCollectionType::class) {
                schemaContext ->
                schemaContext.withDefaultValueRootInstance<SchemaWithQueryMethodWithUnsupportedCollectionType> {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithQueryMethodWithUnsupportedValueType {
        interface CommonConcept

        interface ConceptOne : CommonConcept

        interface ConceptTwo : CommonConcept

        interface ConceptNotCommon

        @References([ConceptOne::class, ConceptTwo::class]) val myConcepts: ConceptNotCommon
    }

    @Test
    fun `test schema with query method with unsupported value type should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongPropertySyntaxException::class,
            SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithQueryMethodWithUnsupportedValueType::class) {
                schemaContext ->
                schemaContext.withDefaultValueRootInstance<SchemaWithQueryMethodWithUnsupportedValueType> {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithConceptWithValidFacets {

        enum class MyEnumeration {
            MY_FIRST_ENUM_VALUE
        }

        interface CommonConcept

        interface SpecificConceptOne : CommonConcept

        interface SpecificConceptTwo : CommonConcept

        val myTextFacetAsListOfString: List<String>

        val myTextFacetAsSetOfString: Set<String>

        val myTextFacetAsString: String

        val myTextFacetAsNullableString: String?

        val myNumberFacetAsListOfInt: List<Int>

        val myNumberFacetAsSetOfInt: Set<Int>

        val myNumberFacetAsInt: Int

        val myNumberFacetAsNullableInt: Int?

        val myBoolFacetAsListOfBoolean: List<Boolean>

        val myBoolFacetAsSetOfBoolean: Set<Boolean>

        val myBoolFacetAsBoolean: Boolean

        val myBoolFacetAsNullableBoolean: Boolean?

        val myEnumerationFacetAsListOfEnums: List<MyEnumeration>

        val myEnumerationFacetAsSetOfEnums: Set<MyEnumeration>

        val myEnumerationFacetAsEnum: MyEnumeration

        val myEnumerationFacetAsNullableEnum: MyEnumeration?

        val mySingleConceptReferenceFacetAsListOfCommonConceptInterface: List<SpecificConceptOne>

        val mySingleConceptReferenceFacetAsSetOfCommonConceptInterface: Set<SpecificConceptOne>

        val mySingleConceptReferenceFacetAsCommonConceptInterface: SpecificConceptOne

        val mySingleConceptReferenceFacetAsNullableCommonConceptInterface: SpecificConceptOne?

        @References([SpecificConceptOne::class, SpecificConceptTwo::class])
        val myMultipleConceptReferenceFacetAsListOfCommonConcept: List<CommonConcept>

        @References([SpecificConceptOne::class, SpecificConceptTwo::class])
        val myMultipleConceptReferenceFacetAsSetOfCommonConcept: Set<CommonConcept>

        @References([SpecificConceptOne::class, SpecificConceptTwo::class])
        val myMultipleConceptReferenceFacetAsCommonConcept: CommonConcept

        @References([SpecificConceptOne::class, SpecificConceptTwo::class])
        val myMultipleConceptReferenceFacetAsNullableCommonConcept: CommonConcept?
    }

    @Test
    fun `test concept with valid return types should return without exception`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithValidFacets::class) { schemaContext ->
            schemaContext.withDefaultValueRootInstance<SchemaWithConceptWithValidFacets> { rootConceptNameAndId ->
                val specificOneRef =
                    schemaContext.dataCollector.newConceptData(
                        SchemaWithConceptWithValidFacets.SpecificConceptOne::class.toConceptName()
                    )
                val specificTwoRef =
                    schemaContext.dataCollector.newConceptData(
                        SchemaWithConceptWithValidFacets.SpecificConceptOne::class.toConceptName()
                    )

                schemaContext.dataCollector
                    .existingConceptData(rootConceptNameAndId.conceptIdentifier)
                    .addFacetValue(
                        SchemaWithConceptWithValidFacets::mySingleConceptReferenceFacetAsCommonConceptInterface
                            .name
                            .toFacetName(),
                        specificOneRef.conceptIdentifier,
                    )
                    .addFacetValue(
                        SchemaWithConceptWithValidFacets::myMultipleConceptReferenceFacetAsCommonConcept
                            .name
                            .toFacetName(),
                        specificTwoRef.conceptIdentifier,
                    )
            }
        }
    }
}
