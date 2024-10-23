package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText


enum class SchemaErrorCode(override val messageFormat: String): ErrorCode {

    MUST_HAVE_ANNOTATION("%s must have an annotation %s."),
    MUST_HAVE_ONE_OF_THE_FOLLOWING_ANNOTATIONS("%s must have must have one of the annotations %s."),
    NOT_MULTIPLE_ANNOTATIONS("%s can not have more than one of the annotations %s."),
    NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS("%s can not have more than %s annotation %s."),
    CAN_NOT_HAVE_ANNOTATION("%s can not have annotation of type %s."),
    CLASS_MUST_BE_AN_INTERFACE("%s must be an interface."),
    CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS("%s must not have extension functions but has %s."),
    CLASS_CANNOT_HAVE_PROPERTIES("%s must not have member properties but has %s."),
    CLASS_CANNOT_HAVE_MEMBER_FUNCTIONS_OR_PROPERTIES("%s must not have any member functions or properties but has %s."),
    RETURN_TYPE_MUST_BE_INHERITABLE("The return type class %s must be inheritable for all concepts %s."),
    FACET_RETURN_TYPE_NOT_SUPPORTED("The method return type for the facet %s was %s which is not a supported value type. Valid return types are %s."),
    RETURN_TYPE_IS_INVALID("%s return type is invalid. %s"),
    RETURN_TYPE_IS_INVALID_ONLY_COLLECTION_OR_CLASS("%s return type is invalid. The return type can only be a class type or a collection (%s) containing a class type."),
    RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS("The collection type of the function must be one of %s but was %s."),
    RETURN_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED("Returning a collection with values marked as nullable (inner generic type) is not allowed."),
    RETURN_TYPE_COLLECTION_TO_FETCH_CONCEPT_IDENTIFIER_NOT_SUPPORTED("The method return type for a method to fetch a concept identifier can not return a collection."),
    RETURN_TYPE_TO_FETCH_CONCEPT_IDENTIFIER_NOT_SUPPORTED("The method return type for a method to fetch a concept identifier was %s, which is not a supported value type. Valid return types are %s."),
    FUNCTION_HAS_RECEIVER_PARAM("%s has extension receiver parameter. This is not allowed."),
    FUNCTION_HAVE_TYPE_PARAMS("%s has type parameters %s. This is not allowed."),
    FUNCTION_MUST_BE_ABSTRACT("%s must be abstract."),
    FUNCTION_MUST_HAVE_RETURN_TYPE("%s must have a return type."),
    FUNCTION_CAN_NOT_HAVE_VALUE_PARAMS("%s has parameters. This is not allowed."),
    FACET_ENUM_INVALID("Facet '%s' on concept '%s' is declared as a enumeration facet but the enumeration is not defined or not a real enumeration class (was '%s')."),
    FACET_ENUM_HAS_PRIVATE_MODIFIER("Facet '%s' on concept '%s' is declared as a enumeration facet ('%s') with a private modifier. Change to public modifier."),
    FACET_REFERENCE_EMPTY_CONCEPT_LIST("Facet '%s' on concept '%s' is declared as a reference facet but the list of concept types is empty."),
    FACET_NOT_REFERENCE_NOT_EMPTY_CONCEPT_LIST("Facet '%s' on concept '%s' is declared is not a reference facet (is '%s') but the list of concept type is not empty (is %s)"),
    FACET_UNKNOWN_REFERENCED_CONCEPT("Facet '%s' on concept '%s' has an reference concept '%s' which is not a known concept (known concepts are '%s')"),
    NO_GENERIC_TYPE_PARAMETER("%s must not have generic type parameters but has type parameters %s."),
    DUPLICATE_CONCEPTS_ON_SCHEMA("There is already a concept registered with name '%s' on schema '%s'. Can not register concept class '%s'."),
    DUPLICATE_FACET_ON_CONCEPT("There is already a facet registered with name '%s' on concept '%s'. Can not register facet class '%s'."),
    NO_NEGATIVE_FACET_CARDINALITIES("Facet '%s' on concept '%s' has negative cardinalities. Only number greater/equal zero are allowed, but was %s/%s."),
    WRONG_FACET_CARDINALITIES("Facet '%s' on concept '%s' has a greater minimumOccurrences (%s) than the maximumOccurrences (%s)."),
    MISSING_QUERY_CONCEPT_ANNOTATION("The method is missing the annotation ${QueryConcepts::class.shortText()}."),
    NO_CONCEPTS_TO_QUERY("The method has an empty list for ${QueryConcepts::conceptClasses.name} on ${QueryConcepts::class.shortText()}."),
    INVALID_CONCEPT_TO_QUERY("The method has a invalid concept class '%s'. Valid concept classes are %s."),
    NO_FACET_TO_QUERY("The method is missing one of the annotations ${QueryFacetValue::class.shortText()} or ${QueryConceptIdentifierValue::class.shortText()}."),
    INVALID_FACET_TO_QUERY("The method has a invalid facet class %s. Valid facet classes are %s."),
    ;
}