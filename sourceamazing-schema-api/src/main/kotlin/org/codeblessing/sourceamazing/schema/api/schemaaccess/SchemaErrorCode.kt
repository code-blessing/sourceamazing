package org.codeblessing.sourceamazing.schema.api.schemaaccess

import org.codeblessing.sourceamazing.schema.api.annotations.AdditionallyKnownClasses
import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCode

enum class SchemaErrorCode(override val messageFormat: String) : ErrorCode {

    ROOT_CLASS_MUST_BE_INSTANTIATABLE(
        "The root class '%s' must be instantiatable.\nThis is not possible for the following reasons:[\n%s\n]"
    ),
    CLASS_MUST_HAVE_A_PRIMARY_CONSTRUCTOR("'%s' must have a primary constructor."),
    CLASS_PRIMARY_CONSTRUCTOR_CAN_NOT_BE_ABSTRACT("'%s' primary constructor can not be abstract."),
    CLASS_PRIMARY_CONSTRUCTOR_CAN_NOT_BE_PRIVATE("'%s' primary constructor can not be private."),
    CLASS_PRIMARY_CONSTRUCTOR_CAN_NOT_HAVE_TYPE_PARAMETERS(
        "'%s' primary constructor can not have type parameters ('generics')."
    ),
    CLASS_PRIMARY_CONSTRUCTOR_PARAMETERS_MUST_BE_VALUE_PARAMETERS(
        "'%s' primary constructor parameters can only be regular value parameters, no extension or instance parameters."
    ),
    CLASS_PRIMARY_CONSTRUCTOR_PARAMETERS_MUST_HAVE_A_NAME("'%s' primary constructor parameters must each have a name."),
    CLASS_MUST_BE_A_REGULAR_CLASS("'%s' must be a regular class and not an enum class, annotation or interface."),
    CLASS_MUST_BE_AN_INTERFACE_OR_AN_INSTANTIABLE_CLASS(
        "'%s' must be an interface or a non-abstract class that can be instantiated."
    ),
    CLAZZ_CAN_NOT_BE_INTERNAL_CLASS(
        "'%s' is a internal class an is prohibited to be used as model as otherwise it is not clear whether you add an instance or a model to the class model collector. All prohibited classes are: %s"
    ),
    CLASS_CANNOT_BE_PRIVATE("'%s' can not be a private class."),
    INTERFACE_CANNOT_HAVE_EXTENSION_FUNCTIONS("'%s' must not have extension functions but has '%s'."),
    INTERFACE_CANNOT_HAVE_MEMBER_FUNCTIONS("'%s' must not have member functions but has '%s'."),
    TYPE_TO_CLASS_DECONSTRUCTION_CLASS_CANNOT_HAVE_GENERIC_TYPE_PARAMETER(
        "'%s' must not have generic type parameters but has type parameters '%s'."
    ),
    CLASS_CANNOT_HAVE_GENERIC_TYPE_PARAMETER("'%s' must not have generic type parameters but has type parameters %s."),
    PROPERTY_HAS_EXTENSION_RECEIVER_PARAM("'%s' has extension receiver parameter. This is not allowed."),
    PROPERTY_HAVE_TYPE_PARAMS("'%s' has type parameters %s. This is not allowed."),
    PROPERTY_MUST_BE_ABSTRACT("'%s' property must be abstract (no get/set body)."),
    CLAZZ_PROPERTY_CANNOT_BE_UNIT_TYPE("'%s' can not be of type ${Unit::class.qualifiedName}."),
    PROPERTY_MUST_NOT_HAVE_EXTENSION_TYPE("The property must not have an extension type."),
    PROPERTY_CAN_NOT_HAVE_VALUE_PARAMS("'%s' has parameters. This is not allowed."),
    CONSTRUCTOR_PARAMETER_CAN_ONLY_BE_VALUE_PARAM(
        "'%s' has a parameters that is not a value parameter but an extension or instance parameter. This is not allowed."
    ),
    CONSTRUCTOR_PARAMETER_CAN_NOT_BE_VARARG_PARAM(
        "'%s' has a parameters that is a vararg parameter. This is not allowed."
    ),
    CONSTRUCTOR_PARAMETER_MUST_BE_A_NAMED_PARAMETER(
        "'%s' has a parameters that has no name an is anonymous. This is not allowed."
    ),
    TYPE_TO_CLASS_DECONSTRUCTION_IS_NOT_AVAILABLE("Type '%s' is invalid."),
    TYPE_TO_CLASS_DECONSTRUCTION_IS_INVALID("Type '%s' is invalid: %s"),
    TYPE_TO_CLASS_DECONSTRUCTION_TYPE_CAN_ONLY_BE_CLASSES("Type '%s' is invalid. Only classes are allowed."),
    CLAZZ_PROPERTY_CLAZZ_TYPE_INVALID("Invalid class type on property/parameter '%s':%s\n  Enclosing element: %s."),
    TYPE_TO_CLASS_DECONSTRUCTION_IS_INVALID_ONLY_COLLECTION_OR_CLASS(
        "The type '%s' can only be a class type or a collection (%s) containing a class type."
    ),
    TYPE_TO_CLASS_DECONSTRUCTION_TYPE_IS_WRONG_COLLECTION_CLASS(
        "The type '%s' is invalid, collection class must be one of %s but was %s."
    ),
    TYPE_TO_CLASS_DECONSTRUCTION_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED(
        "The type '%s' is invalid, a collection with values marked as nullable (inner generic type) is not allowed."
    ),
    TYPE_TO_CLASS_DECONSTRUCTION_TYPE_CANNOT_BE_UNIT_TYPE(
        "The type '%s' is invalid, can not be of type ${Unit::class.qualifiedName}."
    ),
    TYPE_TO_CLASS_DECONSTRUCTION_TYPE_CANNOT_BE_ANNOTATION_TYPE(
        "The type '%s' is invalid, can not be an annotation but was %s."
    ),
    CLAZZ_PROPERTY_ENUM_HAS_PRIVATE_MODIFIER(
        "Property '%s' on class '%s' is declared as a enumeration ('%s') with a private modifier. Change to public modifier."
    ),
    CLAZZ_PROPERTY_CANNOT_HAVE_NEGATIVE_CLAZZ_PROPERTY_CARDINALITIES(
        "Property '%s' on class '%s' has negative cardinalities. Only number greater/equal zero are allowed, but was %s/%s."
    ),
    CLAZZ_PROPERTY_HAS_WRONG_CLAZZ_PROPERTY_CARDINALITIES(
        "Property '%s' on class '%s' has a greater minimumOccurrences (%s) than the maximumOccurrences (%s)."
    ),
    ADDITIONAL_CLAZZS_ANNOTATION_LIST_EMPTY(
        "The annotation @${AdditionallyKnownClasses::class.simpleName} on class '%s' is declared but the list of class types is empty."
    ),
    ADDITIONAL_CLAZZS_ANNOTATION_CONTAINS_DUPLICATES(
        "The annotation @${AdditionallyKnownClasses::class.simpleName} on class '%s' contains duplicates."
    ),
}
