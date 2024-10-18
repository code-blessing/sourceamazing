package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
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
                throw BuilderMethodSyntaxException(method, BuilderErrorCode.MISSING_BUILDER_ANNOTATION)
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
                    method, methodParameter, BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION)
            }

            if(methodParameter.hasAnnotation<InjectBuilder>()) {
                throw BuilderMethodParameterSyntaxException(
                    method, methodParameter,BuilderErrorCode.BUILDER_PARAM_INJECTION_AND_IGNORE_NULL_ANNOTATION)
            }
        }

        if(!isLastParameter) {
            if(!methodParameter.hasAnnotation<SetConceptIdentifierValue>()
                && !methodParameter.hasAnnotation<SetFacetValue>()) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_MISSING_CONCEPT_IDENTIFIER_OR_SET_FACET_ANNOTATION)
            }
        } else {
            if(!methodParameter.hasAnnotation<SetConceptIdentifierValue>()
                && !methodParameter.hasAnnotation<SetFacetValue>()
                && !methodParameter.hasAnnotation<InjectBuilder>()) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_MISSING_CONCEPT_IDENTIFIER_OR_SET_FACET_ANNOTATION_OR_INJECTION)
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
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.UNKNOWN_FACET, annotation.annotationClass.annotationText(), facetClass.longText())

        }
        if(facet.facetType != expectedFacetType) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.WRONG_FACET_TYPE, annotation.annotationClass.annotationText(), facetClass.longText(), expectedFacetType, facet.facetType)
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
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.WRONG_FACET_ENUM_VALUE, annotation.annotationClass.annotationText(), facetClass.longText(), enumValue, validEnumerationValues)
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
                BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_TYPE,
                methodParameter.type
            )
        }
        val typeClass = typeClasses.first()
        if(typeClass.clazz != ConceptIdentifier::class) {
            throw BuilderMethodParameterSyntaxException(
                method,
                methodParameter,
                BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_TYPE,
                typeClass.clazz.longText()
            )
        }
        if(typeClass.isValueNullable) {
            throw BuilderMethodParameterSyntaxException(
                method,
                methodParameter,
                BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_TYPE_NO_NULLABLE
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
            ?: throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_NO_FACET_FOR_CLASS, facetName.clazz.longText())

        when(facetFromSchema.facetType) {
            FacetType.TEXT -> if(typeClass != String::class) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE, facetName)
            }
            FacetType.NUMBER -> if(typeClass != Int::class) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE,facetName)
            }
            FacetType.BOOLEAN -> if(typeClass != Boolean::class) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_WRONG_BOOLEAN_FACET_TYPE,facetName)
            }
            FacetType.TEXT_ENUMERATION -> if(typeClass != facetFromSchema.enumerationType) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE, facetName, facetFromSchema.enumerationType?.shortText() ?: "<unknown-enum>", facetFromSchema.enumerationValues)
            }
            FacetType.REFERENCE -> if(typeClass != ConceptIdentifier::class) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_WRONG_REFERENCE_FACET_TYPE, facetName)
            }
        }

        if(methodParameter.hasAnnotation<IgnoreNullFacetValue>() && !classInformation.isValueNullable) {
            throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_IGNORE_NULL_ANNOTATION_WITHOUT_NULLABLE_TYPE)
        }
        if(!methodParameter.hasAnnotation<IgnoreNullFacetValue>() && classInformation.isValueNullable) {
            throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION)
        }

    }

    private fun validateIsClassParameterType(method: KFunction<*>, methodParameter: KParameter): List<KTypeUtil.KTypeClassInformation> {
        val methodParamType = methodParameter.type

        if(methodParamType.typeKind() == KTypeKind.KCLASS) {
            return KTypeUtil.classesInformationFromKType(methodParamType)
        }

        val detailDescription = when(methodParamType.typeKind()) {
            KTypeKind.FUNCTION -> "Type can only be a class but was '${methodParamType}'."
            KTypeKind.OTHER_TYPE, KTypeKind.TYPE_PARAMETER -> "Type can only be a class but was '${methodParamType}'."
            else -> throw IllegalStateException("Type '${methodParamType.typeKind()}' not supported.")
        }
        val builderErrorCode = if(methodParameter.hasAnnotation<SetConceptIdentifierValue>()) {
            BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_PARAMETER
        } else if(methodParameter.hasAnnotation<InjectBuilder>()) {
            BuilderErrorCode.BUILDER_PARAM_WRONG_INJECTION_PARAMETER
        } else if(methodParameter.hasAnnotation<SetFacetValue>()) {
            BuilderErrorCode.BUILDER_PARAM_WRONG_SET_FACET_VALUE_PARAMETER
        } else {
            BuilderErrorCode.BUILDER_PARAM_WRONG_PARAMETER
        }


        throw BuilderMethodParameterSyntaxException(method, methodParameter, builderErrorCode, detailDescription)
    }

    private fun extractValueClassFromCollectionIfCollection(
        method: KFunction<*>,
        methodParameter: KParameter,
        classesInformation: List<KTypeUtil.KTypeClassInformation>
    ): KTypeUtil.KTypeClassInformation {
        val valueClassOrCollectionClass = classesInformation.first()
        return if(valueClassOrCollectionClass.clazz.starProjectedType in SUPPORTED_COLLECTION_TYPES) {
            if(valueClassOrCollectionClass.isValueNullable) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_NO_NULLABLE_COLLECTION_TYPE)
            }
            classesInformation.last()
        } else {
            valueClassOrCollectionClass
        }
    }
}