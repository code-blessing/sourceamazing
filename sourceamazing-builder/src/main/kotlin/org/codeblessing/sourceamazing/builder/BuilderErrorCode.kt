package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.annotations.BuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.ProvideBuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.schema.ErrorCode
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText

enum class BuilderErrorCode(override val messageFormat: String): ErrorCode {

    MISSING_BUILDER_ANNOTATION("The method is missing the annotation ${BuilderMethod::class.annotationText()}. This annotation must be on every builder method."),
    UNKNOWN_FACET("The method uses an annotation %s with a facet '%s'. The facet is not known. You must register the facet on the concept class using the ${Concept::class.annotationText()} annotation."),
    WRONG_FACET_TYPE("The method uses an annotation %s with a facet '%s'. The facet is not of type '%s' but of type '%s'."),
    WRONG_FACET_ENUM_VALUE("The method uses an annotation %s with a facet '%s', but the facet value '%s' is not a valid enumeration values. Valid enumeration values are: %s."),
    ALIAS_IS_ALREADY_USED("The alias '%s' introduced with the annotation ${NewConcept::class.annotationText()} for concept '%s' is already used. All already used alias names are %s. Choose another alias name. %s"),
    ALIAS_NO_AVAILABLE_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION("The alias '%s' was expected by the annotation ${ExpectedAliasFromSuperiorBuilder::class.annotationText()} but was not provided by the superior builder. %s"),
    DUPLICATE_ALIAS_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION("The alias '%s' was listed multiple times with the ${ExpectedAliasFromSuperiorBuilder::class.annotationText()} annotation. %s"),
    UNKNOWN_CONCEPT("The alias '%s' introduced with the annotation ${NewConcept::class.annotationText()} points to a unknown concept '%s'. You must register the concept on the schema using the ${Schema::class.annotationText()} annotation or use another concept. %s"),
    CONCEPT_HAS_NO_CORRESPONDING_CONCEPT_IDENTIFIER("The concept behind alias '%s' has no corresponding concept identifier declaration. Use the annotation ${SetConceptIdentifierValue::class.annotationText()} or ${SetRandomConceptIdentifierValue::class.annotationText()} to define a concept identifier in the same method as you create the concept (with ${NewConcept::class.annotationText()}). %s"),
    DUPLICATE_SET_RANDOM_CONCEPT_IDENTIFIER_VALUE_USAGE("The concept behind alias '%s' is initialized multiple times with the annotation ${SetRandomConceptIdentifierValue::class.annotationText()}. Remove the duplicate initializations or choose another alias name. %s"),
    DUPLICATE_SET_CONCEPT_IDENTIFIER_VALUE_USAGE("The concept behind alias '%s' is initialized multiple times with the annotation ${SetConceptIdentifierValue::class.annotationText()}. Remove the duplicate initializations or choose another alias name. %s"),
    DUPLICATE_CONCEPT_IDENTIFIER_INITIALIZATION("The concept behind alias '%s' is initialized multiple times with the annotation ${SetConceptIdentifierValue::class.annotationText()} and ${SetRandomConceptIdentifierValue::class.annotationText()}. Do only use one of the annotation per alias. %s"),
    UNKNOWN_ALIAS("The alias '%s' used with the annotation %s is unknown. Choose a known alias name (one of %s) or declare an alias with ${NewConcept::class.annotationText()}. %s"),
    BUILDER_INJECTION_AND_RETURN_AT_SAME_TIME("A builder method can not have an injected builder (with annotation ${InjectBuilder::class.annotationText()}) and at the same time a builder as return type."),
    BUILDER_DECLARED_IN_WITH_NEW_BUILDER_ANNOTATION_MUST_BE_USED("The builder class declared within the annotation ${WithNewBuilder::class.annotationText()} must be returned or injected (with annotation ${InjectBuilder::class.annotationText()})."),
    BUILDER_IN_WITH_NEW_BUILDER_MUST_BE_SAME("The builder class declared within the annotation ${WithNewBuilder::class.annotationText()} must be the same as the return type or the injected builder type of the method."),
    BUILDER_MUST_RETURN_BUILDER_CLASS("The return type of a builder method can only return a builder interface. %s"),
    BUILDER_RETURNED_CAN_NOT_BE_NULLABLE("The return type of a builder method can not be nullable."),
    BUILDER_PARAM_ONLY_LAST_PARAM_CAN_BE_INJECTION("Only the last parameter of the method can have the annotation ${InjectBuilder::class.annotationText()}."),
    BUILDER_PARAM_INJECTION_CANNOT_BE_NULLABLE("An injected builder (parameter with ${InjectBuilder::class.annotationText()}) can not be marked as nullable."),
    BUILDER_PARAM_INJECTION_CANNOT_HAVE_RETURN_TYPE("An injected builder (parameter with ${InjectBuilder::class.annotationText()}) can not have a return type."),
    BUILDER_PARAM_INJECTION_PARAMS_INVALID("An injected builder (parameter with ${InjectBuilder::class.annotationText()}) must have as single parameter a receiver parameter (extension function) type. Its declaration must be \'<Builder>.() -> Unit\'."),
    BUILDER_PARAM_INJECTION_INVALID_RECEIVER_PARAM("The receiver type of the injected builder is invalid. %s"),
    BUILDER_PARAM_INJECTION_NOT_NULLABLE_RECEIVER_PARAM("The receiver type of the injected builder can not be nullable."),
    BUILDER_PARAM_DATA_PROVIDER_CANNOT_BE_NULLABLE("A builder data provider (parameter with ${ProvideBuilderData::class.annotationText()}) can not be marked as nullable."),
    BUILDER_PARAM_DATA_PROVIDER_PARAMS_INVALID("A builder data provider (parameter with ${ProvideBuilderData::class.annotationText()}) can only be a class instance."),
    BUILDER_PARAM_DATA_PROVIDER_AND_IGNORE_NULL_ANNOTATION("A parameter with ${ProvideBuilderData::class.annotationText()} can not have ${IgnoreNullFacetValue::class.annotationText()} at the same time."),
    BUILDER_PARAM_NO_NULLABLE_COLLECTION_TYPE("You can not pass a nullable collection type."),
    BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_TYPE("The parameter of the method to pass a concept identifier (with annotation ${SetConceptIdentifierValue::class.annotationText()}) must be of type '${ConceptIdentifier::class.shortText()}' but was '%s'"),
    BUILDER_PARAM_CONCEPT_IDENTIFIER_TYPE_NO_NULLABLE("The parameter of the method to pass a concept identifier (with annotation ${SetConceptIdentifierValue::class.annotationText()}) can not be a nullable type."),
    BUILDER_PARAM_WRONG_TEXT_FACET_TYPE("To set a value for the text facet '%s', the parameter type must be ${String::class.shortText()} (or a collection %s of ${String::class.shortText()})."),
    BUILDER_PARAM_WRONG_BOOLEAN_FACET_TYPE("To set a value for the boolean facet '%s', the parameter type must be ${Boolean::class.shortText()} (or a collection %s of ${Boolean::class.shortText()})."),
    BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE("To set a value for the number facet '%s', the parameter type must be ${Int::class.shortText()} (or a collection %s of ${Int::class.shortText()})."),
    BUILDER_PARAM_WRONG_ENUM_FACET_TYPE("To set a value for the enumeration facet '%s', the parameter type must be %s (or a collection %s of this enum type) and one of the enumeration values %s. You can also use another enumeration type with a subset of the values."),
    BUILDER_PARAM_WRONG_REFERENCE_FACET_TYPE("To set a value for the reference facet '%s', the parameter type must be ${ConceptIdentifier::class.shortText()} (or a collection %s of ${ConceptIdentifier::class.shortText()})."),
    BUILDER_PARAM_IGNORE_NULL_ANNOTATION_WITHOUT_NULLABLE_TYPE("You can not use ${IgnoreNullFacetValue::class.shortText()} with a parameter that does not have a nullable type."),
    BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION("You can not pass a nullable type. Use ${IgnoreNullFacetValue::class.shortText()} as parameter annotation if you pass a nullable type."),
    BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_PARAMETER("The method parameter to pass a concept identifier (with annotation ${SetConceptIdentifierValue::class.annotationText()}) was wrong. %s"),
    BUILDER_PARAM_WRONG_SET_FACET_VALUE_PARAMETER("The method parameter to set a facet value (with annotation ${SetFacetValue::class.annotationText()}) was wrong. %s"),
    BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION("A parameter setting the concept identifier with ${SetConceptIdentifierValue::class.annotationText()} can not have ${IgnoreNullFacetValue::class.annotationText()} at the same time."),
    BUILDER_PARAM_INJECTION_AND_IGNORE_NULL_ANNOTATION("A parameter with ${InjectBuilder::class.annotationText()} can not have ${IgnoreNullFacetValue::class.annotationText()} at the same time."),
    BUILDER_PARAM_MISSING_CONCEPT_IDENTIFIER_OR_SET_FACET_ANNOTATION("A parameter of the method is missing one of annotations ${SetConceptIdentifierValue::class.annotationText()} or ${SetFacetValue::class.annotationText()} or ${ProvideBuilderData::class.annotationText()}"),
    BUILDER_PARAM_MISSING_CONCEPT_IDENTIFIER_OR_SET_FACET_ANNOTATION_OR_INJECTION("The last parameter of the method is missing one of annotations ${SetConceptIdentifierValue::class.annotationText()}  or ${SetFacetValue::class.annotationText()} or ${InjectBuilder::class.annotationText()} or ${ProvideBuilderData::class.annotationText()}"),
    BUILDER_DATA_PROVIDER_FUNCTION_HAS_PARAMETERS("A builder data provider (method annotated with ${BuilderData::class.annotationText()} can not have parameters."),
    BUILDER_DATA_PROVIDER_FUNCTION_RETURNS_NOTHING("A builder data provider (method annotated with ${BuilderData::class.annotationText()} to set a facet value with ${SetProvidedFacetValue::class.annotationText()} or a concept identifier with ${SetProvidedConceptIdentifierValue::class.annotationText()} must have a matching return type."),
    BUILDER_DATA_PROVIDER_FUNCTION_CAN_NOT_BE_EXTENSION_FUNCTION("A builder data provider (method annotated with ${BuilderData::class.annotationText()}) can not have parameters."),
    ;
}