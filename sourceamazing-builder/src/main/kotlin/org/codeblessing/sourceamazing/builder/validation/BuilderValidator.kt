package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedBooleanFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedEnumFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedIntFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedStringFacetValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodParameterSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.RelevantMethodFetcher
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.toFacetName
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasAnnotation
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasExactNumberOfAnnotations
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoExtensionFunctions
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoGenericTypeParameters
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoProperties
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasOnlyAnnotations
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkIsOrdinaryInterface
import org.codeblessing.sourceamazing.schema.type.KTypeKind
import org.codeblessing.sourceamazing.schema.type.KTypeUtil
import org.codeblessing.sourceamazing.schema.type.getAnnotation
import org.codeblessing.sourceamazing.schema.type.typeKind
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters

object BuilderValidator {
    private const val BUILDER_CLASS_DESCRIPTION = "Builder Class"
    private val SUPPORTED_COLLECTION_TYPES = setOf(List::class.starProjectedType, Set::class.starProjectedType, Array::class.starProjectedType)

    @Throws(SyntaxException::class)
    fun validateBuilderMethods(builderClass: KClass<*>, schemaAccess: SchemaAccess) {
        checkHasOnlyAnnotations(listOf(Builder::class), builderClass, BUILDER_CLASS_DESCRIPTION) // this is only valid for top-level builder
        val allBuilders = collectAllBuilderClasses(builderClass)
        allBuilders.forEach { validateBuilderClassStructureAndMethodSyntax(it, schemaAccess) }
    }

    private fun collectAllBuilderClasses(builderClass: KClass<*>): MutableSet<KClass<*>> {
        val allBuilders = mutableSetOf<KClass<*>>()
        collectBuilderClassesRecursively(allBuilders, builderClass)
        return allBuilders
    }

    private fun collectBuilderClassesRecursively(collectedBuilders: MutableSet<KClass<*>>, builderClass: KClass<*>) {
        validateBuilderClass(builderClass)

        // avoid infinite recursion
        if(!collectedBuilders.contains(builderClass)) {
            collectedBuilders.add(builderClass)
            RelevantMethodFetcher.ownMemberFunctions(builderClass).forEach { method ->
                val subBuilderClass = BuilderClassHelper.getSubBuilderClass(method)
                if(subBuilderClass != null) {
                    collectBuilderClassesRecursively(collectedBuilders, subBuilderClass)
                }
            }
        }
    }

    private fun validateBuilderClassStructureAndMethodSyntax(builderClass: KClass<*>, schemaAccess: SchemaAccess) {
        validateBuilderClass(builderClass)

        RelevantMethodFetcher.ownMemberFunctions(builderClass).forEach { method ->
            if(!method.hasAnnotation<BuilderMethod>()) {
                throw BuilderMethodSyntaxException(
                    method, "The method is missing " +
                            "the annotation ${BuilderMethod::class.annotationText()}. " +
                            "This annotation must be on every builder method."
                )
            }

            BuilderAliasValidator.validateBuilderAlias(builderClass)
            validateCorrectTypesInAnnotations(method, schemaAccess)


            method.valueParameters.forEachIndexed { index, methodParameter ->
                val isLastParameter = index == (method.valueParameters.size - 1)
                validateBuilderMethodParameter(method, methodParameter, schemaAccess, isLastParameter)
            }
        }
    }

    private fun validateBuilderClass(builderClass: KClass<*>) {
        checkIsOrdinaryInterface(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasNoGenericTypeParameters(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasNoExtensionFunctions(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasNoProperties(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasAnnotation(Builder::class, builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasExactNumberOfAnnotations(Builder::class, builderClass, BUILDER_CLASS_DESCRIPTION, numberOf = 1)
        checkHasOnlyAnnotations(listOf(Builder::class, ExpectedAliasFromSuperiorBuilder::class), builderClass, BUILDER_CLASS_DESCRIPTION)
    }

    private fun validateBuilderMethodParameter(method: KFunction<*>, methodParameter: KParameter, schemaAccess: SchemaAccess, isLastParameter: Boolean) {
        validateMethodParameterAnnotations(method, methodParameter, isLastParameter)
        validateCorrectParameterTypes(method, methodParameter, schemaAccess)
    }

    fun validateMethodParameterAnnotations(method: KFunction<*>, methodParameter: KParameter, isLastParameter: Boolean) {
        if(methodParameter.hasAnnotation<IgnoreNullFacetValue>()) {

            if(methodParameter.hasAnnotation<SetConceptIdentifierValue>()) {
                throw BuilderMethodParameterSyntaxException(
                    method, methodParameter,
                    "A parameter setting the" +
                            "concept identifier with ${SetConceptIdentifierValue::class.annotationText()} " +
                            "can not have ${IgnoreNullFacetValue::class.annotationText()} at the same time."
                )
            }

            if(methodParameter.hasAnnotation<InjectBuilder>()) {
                throw BuilderMethodParameterSyntaxException(
                    method, methodParameter,
                    "A parameter with ${InjectBuilder::class.annotationText()} " +
                            "can not have ${IgnoreNullFacetValue::class.annotationText()} at the same time."
                )
            }
        }

        if(!isLastParameter) {
            if(!methodParameter.hasAnnotation<SetConceptIdentifierValue>()
                && !methodParameter.hasAnnotation<SetFacetValue>()) {
                throw BuilderMethodParameterSyntaxException(
                    method, methodParameter,
                    "A parameter of the method " +
                            "is missing one of annotations ${SetConceptIdentifierValue::class.annotationText()} " +
                            "or ${SetFacetValue::class.annotationText()}"
                )
            }
        } else {
            if(!methodParameter.hasAnnotation<SetConceptIdentifierValue>()
                && !methodParameter.hasAnnotation<SetFacetValue>()
                && !methodParameter.hasAnnotation<InjectBuilder>()) {
                throw BuilderMethodParameterSyntaxException(
                    method, methodParameter,
                    "The last parameter of the method " +
                            "is missing one of annotations ${SetConceptIdentifierValue::class.annotationText()} " +
                            "or ${SetFacetValue::class.annotationText()} or ${InjectBuilder::class.annotationText()}"
                )
            }
        }

    }

    private fun validateCorrectTypesInAnnotations(
        method: KFunction<*>,
        schemaAccess: SchemaAccess
    ) {

        method.annotations.filterIsInstance<SetFixedBooleanFacetValue>().forEach { annotation ->
            checkFacetType(
                method = method,
                annotation = annotation,
                facetClass = annotation.facetToModify,
                expectedFacetType = FacetType.BOOLEAN,
                schemaAccess = schemaAccess,
            )
        }

        method.annotations.filterIsInstance<SetFixedIntFacetValue>().forEach { annotation ->
            checkFacetType(
                method = method,
                annotation = annotation,
                facetClass = annotation.facetToModify,
                expectedFacetType = FacetType.NUMBER,
                schemaAccess = schemaAccess,
            )
        }

        method.annotations.filterIsInstance<SetFixedStringFacetValue>().forEach { annotation ->
            checkFacetType(
                method = method,
                annotation = annotation,
                facetClass = annotation.facetToModify,
                expectedFacetType = FacetType.TEXT,
                schemaAccess = schemaAccess,
            )
        }

        method.annotations.filterIsInstance<SetFixedEnumFacetValue>().forEach { annotation ->
            checkFacetEnumTypeAndValue(
                method = method,
                annotation = annotation,
                facetClass = annotation.facetToModify,
                enumValue = annotation.value,
                schemaAccess = schemaAccess,
            )
        }

        method.annotations.filterIsInstance<SetAliasConceptIdentifierReferenceFacetValue>().forEach { annotation ->
            checkFacetType(
                method = method,
                annotation = annotation,
                facetClass = annotation.facetToModify,
                expectedFacetType = FacetType.REFERENCE,
                schemaAccess = schemaAccess,
            )
        }

    }

    private fun checkFacetType(
        method: KFunction<*>,
        annotation: Annotation,
        facetClass: KClass<*>,
        expectedFacetType: FacetType,
        schemaAccess: SchemaAccess,
    ) {
        val facet = schemaAccess.facetByFacetName(facetClass.toFacetName())
        if(facet == null) {
            throw BuilderMethodSyntaxException(
                method,
                "The method uses a ${annotation::class.annotationText()} with " +
                        "a facet '${facetClass.longText()}' that is not known/registered."
            )

        }
        if(facet.facetType != expectedFacetType) {
            throw BuilderMethodSyntaxException(
                method,
                "The method uses an annotation ${annotation::class.shortText()} with " +
                        "a facet '${facetClass.longText()}', but the facet is not a " +
                        "'$expectedFacetType' facet but a '${facet.facetType}' facet."
            )
        }
    }

    private fun checkFacetEnumTypeAndValue(
        method: KFunction<*>,
        annotation: Annotation,
        facetClass: KClass<*>,
        schemaAccess: SchemaAccess,
        enumValue: String,
    ) {
        checkFacetType(
            method = method,
            annotation = annotation,
            facetClass = facetClass,
            expectedFacetType = FacetType.TEXT_ENUMERATION,
            schemaAccess = schemaAccess,
        )

        val facet = requireNotNull(schemaAccess.facetByFacetName(facetClass.toFacetName()))
        val validEnumerationValues = facet.enumerationValues.map { it.name }
        if(!validEnumerationValues.contains(enumValue)) {
            throw BuilderMethodSyntaxException(
                method,
                "The method uses an annotation ${annotation::class.shortText()} with " +
                        "a facet '${facetClass.longText()}', but the facet value '$enumValue' is " +
                        "not a valid enumeration values. Valid values are: ${validEnumerationValues}."
            )
        }
    }

    private fun validateCorrectParameterTypes(
        method: KFunction<*>,
        methodParameter: KParameter,
        schemaAccess: SchemaAccess
    ) {
        if(methodParameter.hasAnnotation<InjectBuilder>()) {
           return
        }
        if(methodParameter.hasAnnotation<SetConceptIdentifierValue>()) {
            validateCorrectConceptIdentifierType(method, methodParameter)
            return
        }
        if(methodParameter.hasAnnotation<SetFacetValue>()) {
            validateCorrectFacetValueType(method, methodParameter, schemaAccess)
            return
        }
    }

    private fun validateCorrectConceptIdentifierType(method: KFunction<*>, methodParameter: KParameter) {
        val exceptionPreamble = "The parameter of the method " +
                "to pass a concept identifier (with annotation ${SetConceptIdentifierValue::class.annotationText()}) "
        val typeClasses = validateIsClassParameterType(method, methodParameter)
        if(typeClasses.size != 1) {
            throw BuilderMethodParameterSyntaxException(
                method,
                methodParameter,
                "$exceptionPreamble must be of type '${ConceptIdentifier::class.shortText()}' but was '${methodParameter.type}'"
            )
        }
        val typeClass = typeClasses.first()
        if(typeClass.clazz != ConceptIdentifier::class) {
            throw BuilderMethodParameterSyntaxException(
                method,
                methodParameter,
                "$exceptionPreamble must be of type '${ConceptIdentifier::class.shortText()}' but was '${typeClass.clazz.longText()}'"
            )
        }
        if(typeClass.isValueNullable) {
            throw BuilderMethodParameterSyntaxException(
                method,
                methodParameter,
                "$exceptionPreamble can not be a nullable type."
            )
        }

    }

    private fun validateCorrectFacetValueType(
        method: KFunction<*>,
        methodParameter: KParameter,
        schemaAccess: SchemaAccess
    ) {

        val classInformation = extractValueClassFromCollectionIfCollection(method, methodParameter, validateIsClassParameterType(method, methodParameter))
        val typeClass = classInformation.clazz

        val facetName = methodParameter.getAnnotation<SetFacetValue>().facetToModify.toFacetName()
        val facetFromSchema = schemaAccess.facetByFacetName(facetName)
            ?: throw BuilderMethodParameterSyntaxException(method, methodParameter, "Could not find facet for class '${facetName.clazz}'")

        when(facetFromSchema.facetType) {
            FacetType.TEXT -> if(typeClass != String::class) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, "To set a value for the text facet '${facetName}', the parameter type must be ${String::class.shortText()}.")
            }
            FacetType.NUMBER -> if(typeClass != Int::class) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, "To set a value for the number facet '${facetName}', the parameter type must be ${Int::class.shortText()}.")
            }
            FacetType.BOOLEAN -> if(typeClass != Boolean::class) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, "To set a value for the boolean facet '${facetName}', the parameter type must be ${Boolean::class.shortText()}.")
            }
            FacetType.TEXT_ENUMERATION -> if(typeClass != facetFromSchema.enumerationType) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, "To set a value for the enumeration facet '${facetName}', the parameter type must be ${facetFromSchema.enumerationType?.shortText()} and one of the enumeration values ${facetFromSchema.enumerationValues}.")
            }
            FacetType.REFERENCE -> if(typeClass != ConceptIdentifier::class) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, "To set a value for the reference facet '${facetName}', the parameter type must be ${ConceptIdentifier::class.shortText()}.")
            }
        }

        if(methodParameter.hasAnnotation<IgnoreNullFacetValue>() && !classInformation.isValueNullable) {
            throw BuilderMethodParameterSyntaxException(method, methodParameter, "You can not use ${IgnoreNullFacetValue::class.shortText()} with a parameter that does not have a nullable type.")
        }
        if(!methodParameter.hasAnnotation<IgnoreNullFacetValue>() && classInformation.isValueNullable) {
            throw BuilderMethodParameterSyntaxException(method, methodParameter, "You can not pass a nullable type. Use ${IgnoreNullFacetValue::class.shortText()} as parameter annotation if you pass a nullable type.")
        }

    }

    private fun validateIsClassParameterType(method: KFunction<*>, methodParameter: KParameter): List<KTypeUtil.KTypeClassInformation> {
        val methodParamType = methodParameter.type

        if(methodParamType.typeKind() == KTypeKind.KCLASS) {
            return KTypeUtil.classesInformationFromKType(methodParamType)
        }

        val parameterWasWrongDescription = parameterWasWrongDescription(methodParameter)
        val detailDescription = when(methodParamType.typeKind()) {
            KTypeKind.FUNCTION -> "Type can only be a class but was '${methodParamType}'."
            KTypeKind.OTHER_TYPE, KTypeKind.TYPE_PARAMETER -> "Type can only be a class but was '${methodParamType}'."
            else -> throw IllegalStateException("Type '${methodParamType.typeKind()}' not supported.")
        }
        throw BuilderMethodParameterSyntaxException(method, methodParameter, "$parameterWasWrongDescription $detailDescription")
    }

    private fun parameterWasWrongDescription(methodParameter: KParameter): String {
        if(methodParameter.hasAnnotation<SetConceptIdentifierValue>()) {
            return "The method parameter '${methodParameter.name}' " +
                    "to pass a concept identifier (with annotation " +
                    "${SetConceptIdentifierValue::class.annotationText()}) " +
                    "was wrong."
        }
        if(methodParameter.hasAnnotation<InjectBuilder>()) {
            return "The method parameter '${methodParameter.name}' " +
                    "to inject a new builder (with annotation " +
                    "${InjectBuilder::class.annotationText()}) " +
                    "was wrong."
        }

        if(methodParameter.hasAnnotation<SetFacetValue>()) {
            return "The method parameter '${methodParameter.name}' " +
                    "to set a facet value (with annotation " +
                    "${SetFacetValue::class.annotationText()}) " +
                    "was wrong."
        }
        return "The method parameter '${methodParameter.name}' was wrong."
    }

    private fun extractValueClassFromCollectionIfCollection(
        method: KFunction<*>,
        methodParameter: KParameter,
        classesInformation: List<KTypeUtil.KTypeClassInformation>
    ): KTypeUtil.KTypeClassInformation {
        val valueClassOrCollectionClass = classesInformation.first()
        return if(valueClassOrCollectionClass.clazz.starProjectedType in SUPPORTED_COLLECTION_TYPES) {
            if(valueClassOrCollectionClass.isValueNullable) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, "You can not pass a nullable collection type.")
            }
            classesInformation.last()
        } else {
            valueClassOrCollectionClass
        }
    }
}