package org.codeblessing.sourceamazing.schema.api.datacollection

import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCode

enum class DataCollectionErrorCode(override val messageFormat: String) : ErrorCode {

    VALIDATION_FAILURES("There are %s validation errors : %s"),
    NON_INSTANTIABLE_CLAZZ(
        "Class '%s' is not instantiable by source amazing.\nThis means that you can only pass instances of this class and can not let them be created.\nThe reasons why this class is not instantiable are the following:\n %s"
    ),
    MISSING_ROOT_CLAZZ("The root class with model id '%s' could not be found."),
    MISSING_REFERENCED_CLAZZ_CLAZZ_PROPERTY_VALUE(
        "Property '%s' of model id '%s' in class '%s' points to a reference that was not found. No model with model id '%s'.\n%s"
    ),
    WRONG_REFERENCED_CLAZZ_CLAZZ_PROPERTY_VALUE(
        "Property '%s' of model id '%s' in class '%s' points to a class that is not permitted. Referenced class was '%s'.\n%s"
    ),
    UNKNOWN_CLAZZ("Class '%s' is not known."),
    DUPLICATE_CLAZZ_IDENTIFIER("The model id '%s' (class: '%s') occurred multiple times. A model id must be unique."),
    UNKNOWN_CLAZZ_PROPERTY(
        "Unknown property name '%s' found for model id '%s' in class '%s'. Known properties are: %s. \n%s"
    ),
    WRONG_CLAZZ_PROPERTY_TYPE(
        "Property '%s' for model id '%s' in class '%s' has a wrong type. A property of type '%s' can not have a value of type '%s' (%s).\n%s"
    ),
    WRONG_CLAZZ_PROPERTY_ENUM_TYPE(
        "Property '%s' for model id '%s' in class '%s' has a wrong type. A property value must be one of %s but was %s (%s).\n%s"
    ),
    MINIMUM_CARDINALITY_ERROR(
        "Property '%s' for model id '%s' in class '%s' has a wrong cardinality. The property must have in minimum %s entries but had %s.\n%s"
    ),
    MAXIMUM_CARDINALITY_ERROR(
        "Property '%s' for model id '%s' in class '%s' has a wrong cardinality. The property must not have more than %s entries but had %s.\n%s"
    ),
    UNRESOLVABLE_CIRCULAR_DEPENDENCY_DETECTED(
        "Found an unresolvable cyclic dependency. A dependency is resolvable if there is in the dependency circle at least one class implemented as interface and not as class.\n%s"
    ),
}
