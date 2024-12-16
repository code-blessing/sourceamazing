package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.alias.BuilderAliasHelper.defaultAliasHint
import org.codeblessing.sourceamazing.builder.alias.BuilderAliasHelper.firstDuplicateAlias
import org.codeblessing.sourceamazing.builder.alias.BuilderAliasHelper.firstMissingAlias
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodParameterSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.builder.facetvalue.EnumFacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.facetvalue.FacetValueAnnotationBaseData
import org.codeblessing.sourceamazing.builder.facetvalue.FacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.facetvalue.ReferenceFacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.validation.BuilderCollectionHelper.SUPPORTED_COLLECTION_TYPES
import org.codeblessing.sourceamazing.builder.validation.BuilderCollectionHelper.extractValueClassFromCollectionIfCollection
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
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
import org.codeblessing.sourceamazing.schema.type.KTypeUtil.KTypeClassInformation
import org.codeblessing.sourceamazing.schema.type.isEnum
import org.codeblessing.sourceamazing.schema.type.receiverParameter
import org.codeblessing.sourceamazing.schema.type.returnTypeOrNull
import org.codeblessing.sourceamazing.schema.type.typeKind
import org.codeblessing.sourceamazing.schema.type.valueParameters
import org.codeblessing.sourceamazing.schema.util.EnumUtil
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.hasAnnotation

object BuilderValidator {
    private const val BUILDER_CLASS_DESCRIPTION = "Builder class"

    fun validateHasOnlyBuilderAnnotation(builderClass: KClass<*>) {
        checkHasOnlyAnnotations(listOf(Builder::class), builderClass, BUILDER_CLASS_DESCRIPTION) // this is only valid for top-level builder
    }

    fun validateBuilderClassStructure(builderClassInterpreter: BuilderClassInterpreter) {
        val builderClass = builderClassInterpreter.builderClass
        checkIsOrdinaryInterface(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasNoGenericTypeParameters(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasNoExtensionFunctions(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasNoProperties(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasAnnotation(Builder::class, builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasExactNumberOfAnnotations(Builder::class, builderClass, BUILDER_CLASS_DESCRIPTION, numberOf = 1)
        checkHasOnlyAnnotations(listOf(Builder::class, ExpectedAliasFromSuperiorBuilder::class), builderClass, BUILDER_CLASS_DESCRIPTION)
    }

    fun validateHasBuilderMethodAnnotation(method: KFunction<*>) {
        if(!method.hasAnnotation<BuilderMethod>()) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.MISSING_BUILDER_ANNOTATION)
        }
    }

    fun validateBuilderMethodReturnType(builderMethodInterpreter: BuilderMethodInterpreter) {
        val method = builderMethodInterpreter.method
        if(method.returnTypeOrNull() == null) {
            return
        }
        val classesInformationFromKType = try {
            KTypeUtil.classesInformationFromKType(method.returnType)
        } catch (ex: IllegalStateException) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.BUILDER_MUST_RETURN_BUILDER_CLASS, ex.message ?:"")
        }
        if(classesInformationFromKType.size != 1) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.BUILDER_MUST_RETURN_BUILDER_CLASS, "")
        }

        val classInformation = classesInformationFromKType.first()
        if(classInformation.isValueNullable) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.BUILDER_RETURNED_CAN_NOT_BE_NULLABLE)
        }
    }

    fun validateBuilderMethodParameter(builderMethodInterpreter: BuilderMethodInterpreter, methodParameter: KParameter, schemaAccess: SchemaAccess, isLastParameter: Boolean) {
        validateInjectBuilderMethodParameterAnnotations(builderMethodInterpreter, methodParameter, isLastParameter)
        validateExpectedMethodParameterAnnotations(builderMethodInterpreter, methodParameter, isLastParameter)
        validateInjectBuilderMethodParamType(builderMethodInterpreter, methodParameter)
        validateIgnoreNullFacetValueMethodParameterAnnotation(builderMethodInterpreter, methodParameter)
    }

    fun validateCorrectConceptIdentifierTypes(builderMethodInterpreter: BuilderMethodInterpreter) {
        builderMethodInterpreter.getManualAssignedConceptIdentifierAnnotationContent().forEach { conceptIdentifierAnnotationData ->
            val methodLocation = conceptIdentifierAnnotationData.methodLocation

            if(conceptIdentifierAnnotationData.ignoreNullValue) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation, BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION)
            }

            val typeClasses = validateIsClassParameterType(conceptIdentifierAnnotationData.type, methodLocation, BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_PARAMETER)
            if(typeClasses.size != 1) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation,
                    BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_TYPE,
                    conceptIdentifierAnnotationData.type
                )
            }
            val typeClass = typeClasses.first()
            if(typeClass.clazz != ConceptIdentifier::class) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation,
                    BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_TYPE,
                    typeClass.clazz.longText()
                )
            }
            if(typeClass.isValueNullable) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation,
                    BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_TYPE_NO_NULLABLE
                )
            }
        }
    }

    private fun validateIgnoreNullFacetValueMethodParameterAnnotation(builderMethodInterpreter: BuilderMethodInterpreter, methodParameter: KParameter) {
        if(methodParameter.hasAnnotation<IgnoreNullFacetValue>()) {
            if(methodParameter.hasAnnotation<InjectBuilder>()) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation = builderMethodInterpreter.createMethodLocation(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_AND_IGNORE_NULL_ANNOTATION,
                )
            }
        }
    }

    private fun validateExpectedMethodParameterAnnotations(builderMethodInterpreter: BuilderMethodInterpreter, methodParameter: KParameter, isLastParameter: Boolean) {
        if(!isLastParameter) {
            if(!methodParameter.hasAnnotation<SetConceptIdentifierValue>()
                && !methodParameter.hasAnnotation<SetFacetValue>()) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation = builderMethodInterpreter.createMethodLocation(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_MISSING_CONCEPT_IDENTIFIER_OR_SET_FACET_ANNOTATION,
                )
            }
        } else {
            if(!methodParameter.hasAnnotation<SetConceptIdentifierValue>()
                && !methodParameter.hasAnnotation<SetFacetValue>()
                && !methodParameter.hasAnnotation<InjectBuilder>()) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation = builderMethodInterpreter.createMethodLocation(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_MISSING_CONCEPT_IDENTIFIER_OR_SET_FACET_ANNOTATION_OR_INJECTION,
                )
            }
        }
    }

    private fun validateInjectBuilderMethodParameterAnnotations(builderMethodInterpreter: BuilderMethodInterpreter, methodParameter: KParameter, isLastParameter: Boolean) {
        if(methodParameter.hasAnnotation<InjectBuilder>()) {
            if(!isLastParameter) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation = builderMethodInterpreter.createMethodLocation(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_ONLY_LAST_PARAM_CAN_BE_INJECTION
                )
            }
        }
    }

    private fun validateInjectBuilderMethodParamType(builderMethodInterpreter: BuilderMethodInterpreter, methodParameter: KParameter) {
        if(methodParameter.hasAnnotation<InjectBuilder>()) {
            val injectionBuilderKType = methodParameter.type
            if (injectionBuilderKType.isMarkedNullable) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation = builderMethodInterpreter.createMethodLocation(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_BE_NULLABLE
                )
            }

            if (injectionBuilderKType.returnTypeOrNull() != null) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation = builderMethodInterpreter.createMethodLocation(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_HAVE_RETURN_TYPE
                )
            }

            val receiverParameterType = injectionBuilderKType.receiverParameter()

            if (receiverParameterType == null || injectionBuilderKType.valueParameters().isNotEmpty()) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation = builderMethodInterpreter.createMethodLocation(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_PARAMS_INVALID
                )
            }

            val receiverParameterKType = try {
                KTypeUtil.kTypeFromProjection(receiverParameterType)
            } catch (ex: IllegalStateException) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation = builderMethodInterpreter.createMethodLocation(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_INVALID_RECEIVER_PARAM,
                    ex.message ?: ""
                )
            }
            if (receiverParameterKType.isMarkedNullable) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation = builderMethodInterpreter.createMethodLocation(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_NOT_NULLABLE_RECEIVER_PARAM
                )
            }

            try {
                KTypeUtil.classFromType(receiverParameterKType)
            } catch (ex: IllegalStateException) {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation = builderMethodInterpreter.createMethodLocation(methodParameter),
                    errorCode = BuilderErrorCode.BUILDER_PARAM_INJECTION_INVALID_RECEIVER_PARAM,
                    ex.message ?: ""
                )
            }
        }
    }

    private fun getTypeClass(facetValueAnnotationContent: FacetValueAnnotationContent): KTypeClassInformation {
        if(facetValueAnnotationContent.base.typeClass != null) {
            return KTypeUtil.classInformationFromClass(facetValueAnnotationContent.base.typeClass, false)
        }
        val methodLocation = facetValueAnnotationContent.base.methodLocation

        val type = facetValueAnnotationContent.base.type
        if(type != null) {
            val classInformationIncludingCollection = validateIsClassParameterType(type, methodLocation, BuilderErrorCode.BUILDER_PARAM_WRONG_SET_FACET_VALUE_PARAMETER)
            val classInformation = extractValueClassFromCollectionIfCollection(classInformationIncludingCollection, methodLocation)
            return classInformation
        }

        throw RuntimeException("Neither the 'type' nor the 'type class' were defined for $facetValueAnnotationContent")
    }

    fun validateCorrectFacetValueTypes(
        builderMethodInterpreter: BuilderMethodInterpreter,
        schemaAccess: SchemaAccess
    ) {
        builderMethodInterpreter.getFacetValueAnnotationContent().forEach { facetValueAnnotationContent ->
            val methodLocation = facetValueAnnotationContent.base.methodLocation
            val facetName = facetValueAnnotationContent.base.facetName
            val facetFromSchema = schemaAccess.facetByFacetName(facetName)
                ?: throw BuilderMethodParameterSyntaxException(methodLocation, BuilderErrorCode.UNKNOWN_FACET, SetFacetValue::class.annotationText(), facetName.clazz.longText())

            val classInformation = getTypeClass(facetValueAnnotationContent)
            val typeClass: KClass<*> = classInformation.clazz

            when(facetFromSchema.facetType) {
                FacetType.TEXT -> if(typeClass != String::class) {
                    throw BuilderMethodParameterSyntaxException(methodLocation, BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE, facetName.clazz.longText(), SUPPORTED_COLLECTION_TYPES)
                }
                FacetType.NUMBER -> if(typeClass != Int::class) {
                    throw BuilderMethodParameterSyntaxException(methodLocation, BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE, facetName.clazz.longText(), SUPPORTED_COLLECTION_TYPES)
                }
                FacetType.BOOLEAN -> if(typeClass != Boolean::class) {
                    throw BuilderMethodParameterSyntaxException(methodLocation, BuilderErrorCode.BUILDER_PARAM_WRONG_BOOLEAN_FACET_TYPE, facetName.clazz.longText(), SUPPORTED_COLLECTION_TYPES)
                }
                FacetType.TEXT_ENUMERATION -> if(!isEnumType(facetFromSchema.enumerationType, typeClass) || !isCompatibleEnum(facetFromSchema.enumerationType, typeClass)) {
                    throw BuilderMethodParameterSyntaxException(methodLocation, BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE, facetName.clazz.longText(), SUPPORTED_COLLECTION_TYPES, facetFromSchema.enumerationType?.shortText() ?: "<unknown-enum>", facetFromSchema.enumerationValues)
                }
                FacetType.REFERENCE -> if(typeClass != ConceptIdentifier::class) {
                    throw BuilderMethodParameterSyntaxException(methodLocation, BuilderErrorCode.BUILDER_PARAM_WRONG_REFERENCE_FACET_TYPE, facetName.clazz.longText(), SUPPORTED_COLLECTION_TYPES)
                }
            }

            if(facetValueAnnotationContent.base.ignoreNullValue && !classInformation.isValueNullable) {
                throw BuilderMethodParameterSyntaxException(methodLocation, BuilderErrorCode.BUILDER_PARAM_IGNORE_NULL_ANNOTATION_WITHOUT_NULLABLE_TYPE)
            }
            if(!facetValueAnnotationContent.base.ignoreNullValue && classInformation.isValueNullable) {
                throw BuilderMethodParameterSyntaxException(methodLocation, BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION)
            }
        }
    }

    private fun isEnumType(enumerationType: KClass<*>?, actualType: KClass<*>): Boolean {
        return enumerationType != null && actualType.isEnum
    }

    private fun isCompatibleEnum(enumerationType: KClass<*>?, actualType: KClass<*>): Boolean {
        return enumerationType != null && EnumUtil.isSameOrSubsetEnumerationClass(fullEnumClass = enumerationType, fullOrSubsetEnumClass = actualType)
    }

    private fun validateIsClassParameterType(type: KType, methodLocation: MethodLocation, builderErrorCode: BuilderErrorCode): List<KTypeUtil.KTypeClassInformation> {
        if(type.typeKind() == KTypeKind.KCLASS) {
            return KTypeUtil.classesInformationFromKType(type)
        }

        val detailDescription = when(type.typeKind()) {
            KTypeKind.FUNCTION -> "Type can only be a class but was '${type}'."
            KTypeKind.OTHER_TYPE, KTypeKind.TYPE_PARAMETER -> "Type can only be a class but was '${type}'."
            else -> throw IllegalStateException("Type '${type.typeKind()}' not supported.")
        }

        throw BuilderMethodParameterSyntaxException(methodLocation, builderErrorCode, detailDescription)
    }

    fun validateAllExpectedAliasesFromSuperiorBuilderAreProvided(builderClassInterpreter: BuilderClassInterpreter) {
        val builderClass = builderClassInterpreter.builderClass
        val expectedAliases = builderClassInterpreter.expectedAliasesFromSuperiorBuilder()
        val providedAliases = builderClassInterpreter.newConceptAliasesFromSuperiorBuilder()
        expectedAliases.forEach { expectedAlias ->
            if(expectedAlias !in providedAliases) {
                throw BuilderSyntaxException(builderClass, BuilderErrorCode.ALIAS_NO_AVAILABLE_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION, expectedAlias, defaultAliasHint(expectedAlias))
            }
        }
    }

    fun validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder(builderClassInterpreter: BuilderClassInterpreter) {
        val builderClass = builderClassInterpreter.builderClass
        val aliasesIncludingDuplicates = builderClassInterpreter.expectedAliasesFromSuperiorBuilderIncludingDuplicates()
        val duplicateAlias = firstDuplicateAlias(aliasesIncludingDuplicates)

        if(duplicateAlias != null) {
            throw BuilderSyntaxException(builderClass, BuilderErrorCode.DUPLICATE_ALIAS_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION, duplicateAlias, defaultAliasHint(duplicateAlias))
        }
    }

    fun validateNoDuplicateAliasInNewConceptAnnotation(builderMethodInterpreter: BuilderMethodInterpreter) {
        val method = builderMethodInterpreter.method
        val aliasesFromSuperiorBuilder: Set<Alias> = builderMethodInterpreter.builderClassInterpreter.expectedAliasesFromSuperiorBuilder()
        val allAliasesFromNewConceptAnnotations = builderMethodInterpreter.newConceptAliasesIncludingDuplicates()
        val allUsedAliasesIncludingDuplicates = aliasesFromSuperiorBuilder.toList() + allAliasesFromNewConceptAnnotations
        val duplicateAlias = firstDuplicateAlias(allUsedAliasesIncludingDuplicates)

        if(duplicateAlias != null) {
            val concept = builderMethodInterpreter.newConceptByAlias(duplicateAlias)
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.ALIAS_IS_ALREADY_USED, duplicateAlias, concept.clazz.longText(),  allUsedAliasesIncludingDuplicates.toSet(), defaultAliasHint(duplicateAlias))
        }
    }

    fun validateConceptIdentifierAssignment(builderMethodInterpreter: BuilderMethodInterpreter) {
        val method: KFunction<*> = builderMethodInterpreter.method
        val aliasesFromNewConceptAssignment: Set<Alias> = builderMethodInterpreter.newConceptAliases()
        // check no duplicate alias in all @SetRandomConceptIdentifierValue
        val setRandomConceptIdentifierAliases = builderMethodInterpreter.aliasesToSetRandomConceptIdentifierValueIncludingDuplicates()
        val duplicateRandomConceptIdentifierAlias = firstDuplicateAlias(setRandomConceptIdentifierAliases)

        if(duplicateRandomConceptIdentifierAlias != null) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.DUPLICATE_SET_RANDOM_CONCEPT_IDENTIFIER_VALUE_USAGE, duplicateRandomConceptIdentifierAlias, defaultAliasHint(duplicateRandomConceptIdentifierAlias))
        }

        // check no duplicate alias in all @SetConceptIdentifierValue
        val setConceptIdentifierValueAliases = builderMethodInterpreter.aliasesToSetConceptIdentifierValueAliasesIncludingDuplicates()
        val duplicateSetConceptIdentifierValueAlias = firstDuplicateAlias(setConceptIdentifierValueAliases)

        if(duplicateSetConceptIdentifierValueAlias != null) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.DUPLICATE_SET_CONCEPT_IDENTIFIER_VALUE_USAGE, duplicateSetConceptIdentifierValueAlias, defaultAliasHint(duplicateSetConceptIdentifierValueAlias))
        }

        // check no duplicate assignment with @SetRandomConceptIdentifierValue and @SetConceptIdentifierValue
        val allConceptIdentifierAssignmentAliases = setRandomConceptIdentifierAliases + setConceptIdentifierValueAliases
        val duplicateAssignmentAlias = firstDuplicateAlias(allConceptIdentifierAssignmentAliases)

        if(duplicateAssignmentAlias != null) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.DUPLICATE_CONCEPT_IDENTIFIER_INITIALIZATION, duplicateAssignmentAlias, defaultAliasHint(duplicateAssignmentAlias))
        }

        // check no missing assignment for all @NewConcept
        val missingConceptIdentifierAssignment = firstMissingAlias(aliasesFromNewConceptAssignment, allConceptIdentifierAssignmentAliases)
        if(missingConceptIdentifierAssignment != null) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.CONCEPT_HAS_NO_CORRESPONDING_CONCEPT_IDENTIFIER, missingConceptIdentifierAssignment, defaultAliasHint(missingConceptIdentifierAssignment))
        }

        // check no unknown aliases in @SetRandomConceptIdentifierValue assignment
        firstMissingAlias(setRandomConceptIdentifierAliases, aliasesFromNewConceptAssignment)?.let { unknownAlias ->
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.UNKNOWN_ALIAS, unknownAlias, SetRandomConceptIdentifierValue::class.annotationText(), aliasesFromNewConceptAssignment, defaultAliasHint(unknownAlias))
        }

        // check no unknown aliases in @SetConceptIdentifierValue assignment
        firstMissingAlias(setConceptIdentifierValueAliases, aliasesFromNewConceptAssignment)?.let { unknownAlias ->
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.UNKNOWN_ALIAS, unknownAlias, SetConceptIdentifierValue::class.annotationText(), aliasesFromNewConceptAssignment, defaultAliasHint(unknownAlias))
        }
    }

    fun validateUsedAliases(builderMethodInterpreter: BuilderMethodInterpreter) {
        val knownValidAliases: Map<Alias, ConceptName> = builderMethodInterpreter.newConceptNamesAndExpectedConceptNamesFromSuperiorBuilder()

        builderMethodInterpreter.getFacetValueAnnotationContent().forEach { facetValue ->
            validateIsValidAlias(facetValue.base, facetValue.base.alias, knownValidAliases)
            if(facetValue is ReferenceFacetValueAnnotationContent && facetValue.referencedAlias != null) {
                validateIsValidAlias(facetValue.base, facetValue.referencedAlias, knownValidAliases)
            }
        }
    }


    fun validateUsedFacets(builderMethodInterpreter: BuilderMethodInterpreter, schemaAccess: SchemaAccess) {
        val knownValidAliases: Map<Alias, ConceptName> = builderMethodInterpreter.newConceptNamesAndExpectedConceptNamesFromSuperiorBuilder()

        builderMethodInterpreter.getFacetValueAnnotationContent().forEach { facetValue ->
            validateIsValidFacet(facetValue.base, knownValidAliases, schemaAccess)
        }
    }

    private fun validateIsValidAlias(
        annotationBaseData: FacetValueAnnotationBaseData,
        alias: Alias,
        knownConceptAliases: Map<Alias, ConceptName>
    ) {

        if(alias !in knownConceptAliases) {
            val method = annotationBaseData.methodLocation.method
            val methodParameter = annotationBaseData.methodLocation.methodParameter
            val annotation = annotationBaseData.annotation

            if(methodParameter == null) {
                throw BuilderMethodSyntaxException(method, BuilderErrorCode.UNKNOWN_ALIAS, alias.name, annotation.annotationClass.annotationText(), knownConceptAliases.keys, defaultAliasHint(alias))
            } else {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation = annotationBaseData.methodLocation,
                    errorCode = BuilderErrorCode.UNKNOWN_ALIAS,
                    alias.name,
                    annotation.annotationClass.annotationText(),
                    knownConceptAliases.keys,
                    defaultAliasHint(alias)
                )
            }
        }
    }

    private fun validateIsValidFacet(
        annotationBaseData: FacetValueAnnotationBaseData,
        knownConceptAliases: Map<Alias, ConceptName>,
        schemaAccess: SchemaAccess
    ) {
        val method = annotationBaseData.methodLocation.method
        val methodParameter = annotationBaseData.methodLocation.methodParameter
        val annotation = annotationBaseData.annotation
        val alias = annotationBaseData.alias
        val facet = annotationBaseData.facetName

        val conceptName = requireNotNull(knownConceptAliases[alias]) {
            "Concept for ${alias.name} does not exist"
        }
        val concept = schemaAccess.conceptByConceptName(conceptName)
        if(!concept.hasFacet(facet)) {
            if(methodParameter == null) {
                throw BuilderMethodSyntaxException(method, BuilderErrorCode.UNKNOWN_FACET, annotation.annotationClass.annotationText(), facet.clazz.longText())
            } else {
                throw BuilderMethodParameterSyntaxException(
                    methodLocation = annotationBaseData.methodLocation,
                    errorCode = BuilderErrorCode.UNKNOWN_FACET,
                    annotation.annotationClass.annotationText(),
                    facet.clazz.longText()
                )
            }
        }
    }

    fun validateKnownConceptsFromNewConceptAnnotation(builderMethodInterpreter: BuilderMethodInterpreter, schemaAccess: SchemaAccess) {
        val method = builderMethodInterpreter.method
        builderMethodInterpreter.newConcepts().forEach { (conceptAlias, conceptName) ->
            if(!schemaAccess.hasConceptName(conceptName)) {
                throw BuilderMethodSyntaxException(method, BuilderErrorCode.UNKNOWN_CONCEPT, conceptAlias, conceptName.clazz.longText(), defaultAliasHint(conceptAlias))
            }
        }
    }


    fun validateCorrectTypesInMethodAnnotations(
        builderMethodInterpreter: BuilderMethodInterpreter,
        schemaAccess: SchemaAccess
    ) {
        builderMethodInterpreter.getFacetValueAnnotationContent().forEach { facetValue ->
            checkFacetType(
                annotationBaseData = facetValue.base,
                expectedFacetType = facetValue.expectedFacetType,
                schemaAccess = schemaAccess,
            )

            if(facetValue is EnumFacetValueAnnotationContent) {
                checkFacetEnumValue(
                    annotationBaseData = facetValue.base,
                    enumValue = facetValue.fixedEnumValue,
                    schemaAccess = schemaAccess,
                )

            }
        }
    }

    private fun checkFacetType(
        annotationBaseData: FacetValueAnnotationBaseData,
        expectedFacetType: FacetType?,
        schemaAccess: SchemaAccess,
    ) {
        val method: KFunction<*> = annotationBaseData.methodLocation.method
        val annotation: Annotation = annotationBaseData.annotation
        val facetClass: KClass<*> = annotationBaseData.facetName.clazz

        val facet = schemaAccess.facetByFacetName(facetClass.toFacetName())
            ?: throw BuilderMethodSyntaxException(method, BuilderErrorCode.UNKNOWN_FACET, annotation.annotationClass.annotationText(), facetClass.longText())

        if(expectedFacetType != null && facet.facetType != expectedFacetType) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.WRONG_FACET_TYPE, annotation.annotationClass.annotationText(), facetClass.longText(), expectedFacetType, facet.facetType)
        }
    }

    private fun checkFacetEnumValue(
        annotationBaseData: FacetValueAnnotationBaseData,
        schemaAccess: SchemaAccess,
        enumValue: String?,
    ) {
        val annotation: Annotation = annotationBaseData.annotation
        val facetClass: KClass<*> = annotationBaseData.facetName.clazz

        val facet = requireNotNull(schemaAccess.facetByFacetName(annotationBaseData.facetName))
        val validEnumerationValues = facet.enumerationValues.map { it.name }

        if(!validEnumerationValues.contains(enumValue)) {
            throw BuilderMethodSyntaxException(
                annotationBaseData.methodLocation.method,
                BuilderErrorCode.WRONG_FACET_ENUM_VALUE,
                annotation.annotationClass.annotationText(),
                facetClass.longText(),
                enumValue ?: "<no-value>",
                validEnumerationValues
            )
        }
    }
}