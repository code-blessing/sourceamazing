package org.codeblessing.sourceamazing.builder.api

import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedClazzModelFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullValue
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewClazzModel
import org.codeblessing.sourceamazing.builder.api.annotations.RedeclareAliasForNestedBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.SetAsClazzModelId
import org.codeblessing.sourceamazing.builder.api.annotations.SetAsValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetClazzModelOfAlias
import org.codeblessing.sourceamazing.builder.api.annotations.SetClazzModelOfId
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedBooleanValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedEnumValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedIntValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedStringValue
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.api.annotations.AdditionallyKnownClasses
import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCode

enum class BuilderErrorCode(override val messageFormat: String) : ErrorCode {

    CLASS_MUST_BE_AN_INTERFACE("Builder class must be an interface."),
    CLASS_CANNOT_BE_PRIVATE("Builder class can not be private. Change to modifier of the class to public or default."),
    CLASS_CANNOT_BE_ANNOTATION("Builder class can not be an annotation interface."),
    CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS("Builder class must not have extension functions but has %s."),
    CLASS_CANNOT_HAVE_PROPERTIES("Builder class must not have member properties but has %s."),
    MUST_HAVE_ANNOTATION("Builder class must have an annotation %s."),
    NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS("Builder class can not have more than %s annotation %s."),
    CAN_NOT_HAVE_ANNOTATION("Builder class can not have annotation of type %s."),
    NO_GENERIC_TYPE_PARAMETER("Builder class must not have generic type parameters but has type parameters %s."),
    MISSING_BUILDER_ANNOTATION(
        "The method is missing the annotation @${BuilderMethod::class.simpleName}. This annotation must be on every builder method."
    ),
    UNKNOWN_CLAZZ_PROPERTY(
        "The method uses an annotation %s with a class property '%s'. The class property is not known. You must register the class property on the class class by declaring a property referencing this class."
    ),
    WRONG_CLAZZ_PROPERTY_TYPE(
        "The method uses an annotation %s with a class property '%s'. The class property expects type '%s' but the value '%s' was not this type."
    ),
    WRONG_CLAZZ_PROPERTY_ENUM_VALUE(
        "The method uses an annotation %s with a class property '%s', but the class property value '%s' is not a valid enumeration values. Valid enumeration values are: %s."
    ),
    ALIAS_IS_ALREADY_USED(
        "The alias '%s' introduced with the annotation @${NewClazzModel::class.simpleName} for class '%s' is already used. All already used alias names are %s. Choose another alias name."
    ),
    UNKNOWN_REDECLARATION_ALIAS(
        "The alias '%s' mentioned with the annotation @${RedeclareAliasForNestedBuilder::class.simpleName} must be a previously already declared alias. All already declared alias names are %s. Choose one of these alias names."
    ),
    ALIAS_NO_AVAILABLE_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION(
        "The alias '%s' was expected by the annotation @${ExpectedClazzModelFromSuperiorBuilder::class.simpleName} but was not provided by the superior builder."
    ),
    EXPECTED_CLAZZ_FROM_SUPERIOR_BUILDER_ANNOTATION_NOT_MATCHING_PROVIDED_CLAZZ(
        "The alias '%s' from the annotation @${ExpectedClazzModelFromSuperiorBuilder::class.simpleName} expects a class '%s' but '%s' was provided by the superior builder."
    ),
    DUPLICATE_ALIAS_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION(
        "The alias '%s' was listed multiple times with the @${ExpectedClazzModelFromSuperiorBuilder::class.simpleName} annotation."
    ),
    UNKNOWN_CLAZZ(
        "The alias '%s' introduced with the annotation @${NewClazzModel::class.simpleName} points to a unknown class '%s'. You must register the class on the schema by declaring a property referencing this class or using the @${AdditionallyKnownClasses::class.simpleName} annotation."
    ),
    NOT_INSTANTIATABLE_CLAZZ(
        "The alias '%s' introduced with the annotation @${NewClazzModel::class.simpleName} points to a class '%s' that can not be instantiate. Reasons: [\n%s\n]"
    ),
    DUPLICATE_SET_CLAZZ_IDENTIFIER_VALUE_USAGE(
        "The class behind alias '%s' is initialized multiple times with the annotation @${SetAsClazzModelId::class.simpleName}. Remove the duplicate initializations or choose another alias name."
    ),
    UNKNOWN_ALIAS(
        "The alias '%s' used with the annotation %s is unknown. Choose a known alias name (one of %s) or declare an alias with @${NewClazzModel::class.simpleName}."
    ),
    BUILDER_MUST_RETURN_BUILDER_CLASS("The return type of a builder method can only return a builder interface. %s"),
    BUILDER_RETURNED_CAN_NOT_BE_NULLABLE("The return type of a builder method can not be nullable."),
    BUILDER_PARAM_ONLY_LAST_PARAM_CAN_BE_INJECTION(
        "Only the last parameter of the method can have the annotation @${InjectBuilder::class.simpleName}."
    ),
    BUILDER_PARAM_INJECTION_CANNOT_BE_NULLABLE(
        "An injected builder (parameter with @${InjectBuilder::class.simpleName}) can not be marked as nullable."
    ),
    BUILDER_PARAM_INJECTION_CANNOT_HAVE_RETURN_TYPE(
        "An injected builder (parameter with @${InjectBuilder::class.simpleName}) can not have a return type."
    ),
    BUILDER_PARAM_INJECTION_PARAMS_INVALID(
        "An injected builder (parameter with @${InjectBuilder::class.simpleName}) must have as single parameter a receiver parameter (extension function) type. Its declaration must be \'<Builder>.() -> Unit\'."
    ),
    BUILDER_PARAM_INJECTION_INVALID_RECEIVER_PARAM("The receiver type of the injected builder is invalid. %s"),
    BUILDER_PARAM_INJECTION_NOT_NULLABLE_RECEIVER_PARAM(
        "The receiver type of the injected builder can not be nullable."
    ),
    BUILDER_PARAM_NO_NULLABLE_COLLECTION_TYPE("You can not pass a nullable collection type."),
    BUILDER_PARAM_WRONG_CLAZZ_IDENTIFIER_TYPE(
        "The parameter of the method to pass a class identifier (with annotation @${SetAsClazzModelId::class.simpleName}) must be  class type but was '%s'"
    ),
    BUILDER_PARAM_CLAZZ_IDENTIFIER_TYPE_NO_NULLABLE(
        "The parameter of the method to pass a class identifier (with annotation @${SetAsClazzModelId::class.simpleName}) can not be a nullable type."
    ),
    BUILDER_PARAM_WRONG_ENUM_CLAZZ_PROPERTY_TYPE(
        "To set a value for the enumeration class property '%s', the parameter type must be %s (or a collection %s of this enum type) and one of the enumeration values %s. %s"
    ),
    BUILDER_PARAM_WRONG_CLAZZ_PROPERTY_TYPE(
        "To set a value for the reference class property '%s', the parameter type must be %s (or a subclass of) or a collection %s of it. %s"
    ),
    BUILDER_PARAM_IGNORE_NULL_ANNOTATION_WITHOUT_NULLABLE_TYPE(
        "You can not use @${IgnoreNullValue::class.simpleName} with a parameter that does not have a nullable type."
    ),
    BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION(
        "You can not pass a nullable type. Use @${IgnoreNullValue::class.simpleName} as parameter annotation if you pass a nullable type."
    ),
    BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER(
        "The method parameter to set a class property value (with annotation @${SetAsValue::class.simpleName}) was wrong. %s"
    ),
    BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_REFERENCE_PARAMETER(
        "The method parameter to set a class property reference (with annotation @${SetClazzModelOfId::class.simpleName}) was wrong. %s"
    ),
    BUILDER_WRONG_FIXED_CLAZZ_PROPERTY_VALUE_ANNOTATION_PARAM(
        "The value provided with a fixed value annotation (most often @${SetFixedEnumValue::class.simpleName}, but also @${SetFixedStringValue::class.simpleName}, @${SetFixedIntValue::class.simpleName}, @${SetFixedBooleanValue::class.simpleName}) was wrong. %s"
    ),
    BUILDER_PARAM_WRONG_REFERENCE_VALUE_PARAM(
        "The method param to set a class property value as a reference (with annotation @${SetClazzModelOfAlias::class.simpleName}) was wrong. %s"
    ),
    BUILDER_PARAM_CLAZZ_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION(
        "A parameter setting the class identifier with @${SetAsClazzModelId::class.simpleName} can not have @${IgnoreNullValue::class.simpleName} at the same time."
    ),
    BUILDER_PARAM_INJECTION_AND_IGNORE_NULL_ANNOTATION(
        "A parameter with @${InjectBuilder::class.simpleName} can not have @${IgnoreNullValue::class.simpleName} at the same time."
    ),
    BUILDER_PARAM_MISSING_CLAZZ_IDENTIFIER_OR_SET_CLAZZ_PROPERTY_ANNOTATION(
        "A parameter of the method is missing one of annotations @${SetAsClazzModelId::class.simpleName} or @${SetAsValue::class.simpleName} or @${SetClazzModelOfId::class.simpleName}"
    ),
    BUILDER_PARAM_MISSING_CLAZZ_IDENTIFIER_OR_SET_CLAZZ_PROPERTY_ANNOTATION_OR_INJECTION(
        "The last parameter of the method is missing one of annotations @${SetAsClazzModelId::class.simpleName}  or @${SetAsValue::class.simpleName} or @${SetClazzModelOfId::class.simpleName} or @${InjectBuilder::class.simpleName}"
    ),
    BUILDER_IMPLEMENTATION_MUST_INHERIT_FROM_BUILDER_INTERFACE(
        "A builder implementation class must inherit from its builder interface."
    ),
    BUILDER_FACTORY_DUPLICATES_FOUND("There were duplicates found for the factory of the interface '%s'."),
    BUILDER_IMPLEMENTATION_MUST_NOT_BE_ABSTRACT(
        "A builder implementation class must not be abstract or sealed or companion or inner class."
    ),
    BUILDER_IMPLEMENTATION_MUST_HAVE_A_VALID_CONSTRUCTOR(
        "A builder implementation class must have a valid constructor. A no argument constructor or only arguments of the builder class itself or the class '${SchemaContext::class.simpleName}' or '${BuilderContext::class.simpleName}'."
    ),
    BUILDER_WITH_NON_BUILDER_METHODS_MUST_HAVE_BUILDER_IMPLEMENTATION(
        "A builder with methods without the @${BuilderMethod::class.simpleName} annotation must be implemented by an implementation class (the class must be declared with help of the BuilderFactory) or implemented by a default method."
    ),
    NON_BUILDER_METHODS_CAN_NOT_HAVE_BUILDER_ANNOTATIONS(
        "A methods without the @${BuilderMethod::class.simpleName} annotation can not have builder annotations on the method or its parameters as they wont be evaluated."
    ),
    BUILDER_IMPLEMENTATION_CAN_NOT_HAVE_BUILDER_ANNOTATIONS(
        "A builder implementation class can not have builder annotations on the class, method or its parameters as they wont be evaluated. %s"
    ),
    CLAZZ_PROPERTY_INCOMPATIBLE_REFERENCE_TYPE(
        "Property '%s' on class '%s' has class %s but the provided class was %s."
    ),
    CLAZZ_PROPERTY_INCOMPATIBLE_TYPE("Property '%s' on class '%s' has class %s but the provided class was %s."),
}
