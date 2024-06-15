package org.codeblessing.sourceamazing.schema


enum class DataCollectionErrorCode(override val messageFormat: String): ErrorCode {

    MULTIPLE_DATA_VALIDATION_EXCEPTIONS("There where %s data validation exceptions."),
    MISSING_REFERENCED_CONCEPT_FACET_VALUE("Facet '%s' of concept identifier '%s' in concept '%s' points to a reference that was not found. No concept with concept id '%s'. Must be one of these concepts: %s. \n%s"),
    WRONG_REFERENCED_CONCEPT_FACET_VALUE("Facet '%s' of concept identifier '%s' in concept '%s' points to a concept that is not permitted. Referenced concept was '%s'. Must be one of these concepts: %s. \n%s"),
    UNKNOWN_CONCEPT("The entry with the identifier '%s' points to a concept '%s' that is not known. \n%s"),
    DUPLICATE_CONCEPT_IDENTIFIER("The identifier '%s' (concept: '%s') occurred multiple times. A concept identifier must be unique. \n%s"),
    UNKNOWN_FACET("Unknown facet name '%s' found for concept identifier '%s' in concept '%s'. Known facets are: %s. \n%s"),
    WRONG_FACET_TYPE("Facet '%s' for concept identifier '%s' in concept '%s' has a wrong type. A facet of type '%s' can not have a value of type '%s' (%s).\n%s"),
    WRONG_FACET_ENUM_TYPE("Facet '%s' for concept identifier '%s' in concept '%s' has a wrong type. A facet value must be one of %s but was %s (%s).\n%s"),
    MINIMUM_CARDINALITY_ERROR("Facet '%s' for concept identifier '%s' in concept '%s' has a wrong cardinality. The facet must have in minimum %s entries but had %s.\n%s"),
    MAXIMUM_CARDINALITY_ERROR("Facet '%s' for concept identifier '%s' in concept '%s' has a wrong cardinality. The facet must not have more than %s entries but had %s.\n%s"),
    ;
}