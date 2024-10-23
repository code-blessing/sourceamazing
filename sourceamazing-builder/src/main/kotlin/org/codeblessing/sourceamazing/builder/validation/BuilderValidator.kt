package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.alias.BuilderAliasHelper.allAliasesFromExpectedAliasFromSuperiorBuilderAnnotations
import org.codeblessing.sourceamazing.builder.alias.BuilderAliasHelper.defaultAliasHint
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedBooleanFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedEnumFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedIntFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedStringFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodParameterSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.toConceptName
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
import org.codeblessing.sourceamazing.schema.type.isEnum
import org.codeblessing.sourceamazing.schema.type.receiverParameter
import org.codeblessing.sourceamazing.schema.type.returnTypeOrNull
import org.codeblessing.sourceamazing.schema.type.typeKind
import org.codeblessing.sourceamazing.schema.type.valueParameters
import org.codeblessing.sourceamazing.schema.util.EnumUtil
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters

object BuilderValidator {
    private const val BUILDER_CLASS_DESCRIPTION = "Builder class"
    private val SUPPORTED_COLLECTION_TYPES = setOf(List::class.starProjectedType, Set::class.starProjectedType, Array::class.starProjectedType)

    fun validateHasOnlyBuilderAnnotation(builderClass: KClass<*>) {
        checkHasOnlyAnnotations(listOf(Builder::class), builderClass, BUILDER_CLASS_DESCRIPTION) // this is only valid for top-level builder
    }

    fun validateBuilderClassStructure(builderClass: KClass<*>) {
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

    fun validateMethodReturnType(method: KFunction<*>) {
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

    fun validateIgnoreNullFacetValueMethodParameterAnnotation(method: KFunction<*>, methodParameter: KParameter) {
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
    }

    fun validateExpectedMethodParameterAnnotations(method: KFunction<*>, methodParameter: KParameter, isLastParameter: Boolean) {
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

    fun validateInjectBuilderMethodParameterAnnotations(method: KFunction<*>, methodParameter: KParameter, isLastParameter: Boolean) {
        if(methodParameter.hasAnnotation<InjectBuilder>()) {
            if(!isLastParameter) {
                throw BuilderMethodParameterSyntaxException(
                    method,
                    methodParameter,
                    BuilderErrorCode.BUILDER_PARAM_ONLY_LAST_PARAM_CAN_BE_INJECTION
                )
            }
        }
    }

    fun validateInjectBuilderMethodParamType(method: KFunction<*>, methodParameter: KParameter) {
        if(methodParameter.hasAnnotation<InjectBuilder>()) {
            val injectionBuilderKType = methodParameter.type
            if (injectionBuilderKType.isMarkedNullable) {
                throw BuilderMethodParameterSyntaxException(
                    method,
                    methodParameter,
                    BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_BE_NULLABLE
                )
            }

            if (injectionBuilderKType.returnTypeOrNull() != null) {
                throw BuilderMethodParameterSyntaxException(
                    method,
                    methodParameter,
                    BuilderErrorCode.BUILDER_PARAM_INJECTION_CANNOT_HAVE_RETURN_TYPE
                )
            }

            val receiverParameterType = injectionBuilderKType.receiverParameter()

            if (receiverParameterType == null || injectionBuilderKType.valueParameters().isNotEmpty()) {
                throw BuilderMethodParameterSyntaxException(
                    method,
                    methodParameter,
                    BuilderErrorCode.BUILDER_PARAM_INJECTION_PARAMS_INVALID
                )
            }

            val receiverParameterKType = try {
                KTypeUtil.kTypeFromProjection(receiverParameterType)
            } catch (ex: IllegalStateException) {
                throw BuilderMethodParameterSyntaxException(
                    method,
                    methodParameter,
                    BuilderErrorCode.BUILDER_PARAM_INJECTION_INVALID_RECEIVER_PARAM,
                    ex.message ?: ""
                )
            }
            if (receiverParameterKType.isMarkedNullable) {
                throw BuilderMethodParameterSyntaxException(
                    method,
                    methodParameter,
                    BuilderErrorCode.BUILDER_PARAM_INJECTION_NOT_NULLABLE_RECEIVER_PARAM
                )
            }

            try {
                KTypeUtil.classFromType(receiverParameterKType)
            } catch (ex: IllegalStateException) {
                throw BuilderMethodParameterSyntaxException(
                    method,
                    methodParameter,
                    BuilderErrorCode.BUILDER_PARAM_INJECTION_INVALID_RECEIVER_PARAM,
                    ex.message ?: ""
                )
            }
        }
    }

    fun validateCorrectFacetValueType(
        method: KFunction<*>,
        methodParameter: KParameter,
        schemaAccess: SchemaAccess
    ) {

        val classInformation = extractValueClassFromCollectionIfCollection(method, methodParameter,
            validateIsClassParameterType(method, methodParameter)
        )
        val typeClass = classInformation.clazz

        val facetName = methodParameter.getAnnotation<SetFacetValue>().facetToModify.toFacetName()
        val facetFromSchema = schemaAccess.facetByFacetName(facetName)
            ?: throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.UNKNOWN_FACET, SetFacetValue::class.annotationText(), facetName.clazz.longText())

        when(facetFromSchema.facetType) {
            FacetType.TEXT -> if(typeClass != String::class) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE, facetName.clazz.longText(), SUPPORTED_COLLECTION_TYPES)
            }
            FacetType.NUMBER -> if(typeClass != Int::class) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE, facetName.clazz.longText(), SUPPORTED_COLLECTION_TYPES)
            }
            FacetType.BOOLEAN -> if(typeClass != Boolean::class) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_WRONG_BOOLEAN_FACET_TYPE, facetName.clazz.longText(), SUPPORTED_COLLECTION_TYPES)
            }
            FacetType.TEXT_ENUMERATION -> if(!isEnumType(facetFromSchema.enumerationType, typeClass) || !isCompatibleEnum(facetFromSchema.enumerationType, typeClass)) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE, facetName.clazz.longText(), SUPPORTED_COLLECTION_TYPES, facetFromSchema.enumerationType?.shortText() ?: "<unknown-enum>", facetFromSchema.enumerationValues)
            }
            FacetType.REFERENCE -> if(typeClass != ConceptIdentifier::class) {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_WRONG_REFERENCE_FACET_TYPE, facetName.clazz.longText(), SUPPORTED_COLLECTION_TYPES)
            }
        }

        if(methodParameter.hasAnnotation<IgnoreNullFacetValue>() && !classInformation.isValueNullable) {
            throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_IGNORE_NULL_ANNOTATION_WITHOUT_NULLABLE_TYPE)
        }
        if(!methodParameter.hasAnnotation<IgnoreNullFacetValue>() && classInformation.isValueNullable) {
            throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION)
        }
    }

    private fun isEnumType(enumerationType: KClass<*>?, actualType: KClass<*>): Boolean {
        return enumerationType != null && actualType.isEnum
    }

    private fun isCompatibleEnum(enumerationType: KClass<*>?, actualType: KClass<*>): Boolean {
        return enumerationType != null && EnumUtil.isSameOrSubsetEnumerationClass(fullEnumClass = enumerationType, fullOrSubsetEnumClass = actualType)
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

    fun validateCorrectConceptIdentifierType(method: KFunction<*>, methodParameter: KParameter) {
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

    fun validateAllExpectedAliasesFromSuperiorBuilderAreProvided(builderClass: KClass<*>, newConceptsFromSuperiorMethod: Map<Alias, ConceptName>) {
        val expectedAliases = allAliasesFromExpectedAliasFromSuperiorBuilderAnnotations(builderClass).toSet()
        val providedAliases = newConceptsFromSuperiorMethod.keys
        expectedAliases.forEach { expectedAlias ->
            if(expectedAlias !in providedAliases) {
                throw BuilderSyntaxException(builderClass, BuilderErrorCode.ALIAS_NO_AVAILABLE_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION, expectedAlias, defaultAliasHint(expectedAlias))
            }
        }
    }

    fun validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder(builderClass: KClass<*>) {
        val aliases = allAliasesFromExpectedAliasFromSuperiorBuilderAnnotations(builderClass)
        val duplicateAlias = firstDuplicateAlias(aliases)

        if(duplicateAlias != null) {
            throw BuilderSyntaxException(builderClass, BuilderErrorCode.DUPLICATE_ALIAS_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION, duplicateAlias, defaultAliasHint(duplicateAlias))
        }
    }

    fun validateNoDuplicateAliasInNewConceptAnnotation(method: KFunction<*>, aliasesFromSuperiorBuilder: Set<Alias>) {
        val alreadyUsedAliases: MutableSet<Alias> = aliasesFromSuperiorBuilder.toMutableSet()
        val allNewConceptAnnotations = method.annotations.filterIsInstance<NewConcept>()
        val allAliasesFromNewConceptAnnotations = allNewConceptAnnotations.map { it.declareConceptAlias.toAlias()}
        val duplicateAlias = firstDuplicateAlias(alreadyUsedAliases.toList() + allAliasesFromNewConceptAnnotations)

        if(duplicateAlias != null) {
            val allUsedAliases = aliasesFromSuperiorBuilder + allAliasesFromNewConceptAnnotations
            val concept = allNewConceptAnnotations
                .filter { it.declareConceptAlias.toAlias() == duplicateAlias}
                .map { it.concept.toConceptName() }
                .last()
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.ALIAS_IS_ALREADY_USED, duplicateAlias, concept.clazz.longText(),  allUsedAliases, defaultAliasHint(duplicateAlias))
        }
    }

    fun validateConceptIdentifierAssignment(method: KFunction<*>, aliasesFromNewConceptAssignment: Set<Alias>) {
        // check no duplicate alias in all @SetRandomConceptIdentifierValue
        val setRandomConceptIdentifierAliases = method.annotations
            .filterIsInstance<SetRandomConceptIdentifierValue>()
            .map { it.conceptToModifyAlias.toAlias()}
        val duplicateRandomConceptIdentifierAlias = firstDuplicateAlias(setRandomConceptIdentifierAliases)

        if(duplicateRandomConceptIdentifierAlias != null) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.DUPLICATE_SET_RANDOM_CONCEPT_IDENTIFIER_VALUE_USAGE, duplicateRandomConceptIdentifierAlias, defaultAliasHint(duplicateRandomConceptIdentifierAlias))
        }

        // check no duplicate alias in all @SetConceptIdentifierValue
        val setConceptIdentifierValueAliases = method
            .valueParameters
            .flatMap { parameter -> parameter.annotations.filterIsInstance<SetConceptIdentifierValue>() }
            .map { it.conceptToModifyAlias.toAlias()}
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

    fun validateUsedAliasesAndFacets(method: KFunction<*>, knownAliases: Map<Alias, ConceptName>, schemaAccess: SchemaAccess) {
        method.annotations.filterIsInstance<SetFixedBooleanFacetValue>().forEach { annotation ->
            val alias = annotation.conceptToModifyAlias.toAlias()
            val facet = annotation.facetToModify.toFacetName()
            validateIsValidAlias(method, methodParameter=null, annotation, alias, knownAliases)
            validateIsValidFacet(method, methodParameter=null, annotation, alias, knownAliases, facet, schemaAccess)
        }

        method.annotations.filterIsInstance<SetFixedEnumFacetValue>().forEach { annotation ->
            val alias = annotation.conceptToModifyAlias.toAlias()
            val facet = annotation.facetToModify.toFacetName()
            validateIsValidAlias(method, methodParameter=null, annotation, alias, knownAliases)
            validateIsValidFacet(method, methodParameter=null, annotation, alias, knownAliases, facet, schemaAccess)
        }

        method.annotations.filterIsInstance<SetFixedIntFacetValue>().forEach { annotation ->
            val alias = annotation.conceptToModifyAlias.toAlias()
            val facet = annotation.facetToModify.toFacetName()
            validateIsValidAlias(method, methodParameter=null, annotation, alias, knownAliases)
            validateIsValidFacet(method, methodParameter=null, annotation, alias, knownAliases, facet, schemaAccess)
        }

        method.annotations.filterIsInstance<SetFixedStringFacetValue>().forEach { annotation ->
            val alias = annotation.conceptToModifyAlias.toAlias()
            val facet = annotation.facetToModify.toFacetName()
            validateIsValidAlias(method, methodParameter=null, annotation, alias, knownAliases)
            validateIsValidFacet(method, methodParameter=null, annotation, alias, knownAliases, facet, schemaAccess)
        }

        method.annotations.filterIsInstance<SetAliasConceptIdentifierReferenceFacetValue>().forEach { annotation ->
            val alias = annotation.conceptToModifyAlias.toAlias()
            val referencedAlias = annotation.referencedConceptAlias.toAlias()
            val facet = annotation.facetToModify.toFacetName()
            validateIsValidAlias(method, methodParameter=null, annotation, alias, knownAliases)
            validateIsValidAlias(method, methodParameter=null, annotation, referencedAlias, knownAliases)
            validateIsValidFacet(method, methodParameter=null, annotation, alias, knownAliases, facet, schemaAccess)
        }


        method.valueParameters.forEach { methodParameter ->
            methodParameter.annotations.filterIsInstance<SetFacetValue>().forEach { annotation ->
                val alias = annotation.conceptToModifyAlias.toAlias()
                val facet = annotation.facetToModify.toFacetName()
                validateIsValidAlias(method, methodParameter=methodParameter, annotation, alias, knownAliases)
                validateIsValidFacet(method, methodParameter=methodParameter, annotation, alias, knownAliases, facet, schemaAccess)
            }
        }
    }

    private fun validateIsValidAlias(
        method: KFunction<*>,
        methodParameter: KParameter?,
        annotation: Annotation,
        alias: Alias,
        knownConceptAliases: Map<Alias, ConceptName>
    ) {
        if(alias !in knownConceptAliases) {
            if(methodParameter == null) {
                throw BuilderMethodSyntaxException(method, BuilderErrorCode.UNKNOWN_ALIAS, alias.name, annotation.annotationClass.annotationText(), knownConceptAliases.keys, defaultAliasHint(alias))
            } else {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.UNKNOWN_ALIAS, alias.name, annotation.annotationClass.annotationText(), knownConceptAliases.keys, defaultAliasHint(alias))
            }
        }
    }

    private fun validateIsValidFacet(
        method: KFunction<*>,
        methodParameter: KParameter?,
        annotation: Annotation,
        alias: Alias,
        knownConceptAliases: Map<Alias, ConceptName>,
        facet: FacetName,
        schemaAccess: SchemaAccess
    ) {
        val conceptName = requireNotNull(knownConceptAliases[alias]) {
            "Concept for ${alias.name} does not exist"
        }
        val concept = schemaAccess.conceptByConceptName(conceptName)
        if(!concept.hasFacet(facet)) {
            if(methodParameter == null) {
                throw BuilderMethodSyntaxException(method, BuilderErrorCode.UNKNOWN_FACET, annotation.annotationClass.annotationText(), facet.clazz.longText())
            } else {
                throw BuilderMethodParameterSyntaxException(method, methodParameter, BuilderErrorCode.UNKNOWN_FACET, annotation.annotationClass.annotationText(), facet.clazz.longText())
            }
        }
    }

    fun validateKnownConceptsFromNewConceptAnnotation(method: KFunction<*>, schemaAccess: SchemaAccess) {
        method.annotations.filterIsInstance<NewConcept>().forEach { newConceptAnnotation ->
            val conceptAlias = newConceptAnnotation.declareConceptAlias.toAlias()
            val conceptName = newConceptAnnotation.concept.toConceptName()

            if(!schemaAccess.hasConceptName(conceptName)) {
                throw BuilderMethodSyntaxException(method, BuilderErrorCode.UNKNOWN_CONCEPT, conceptAlias, conceptName.clazz.longText(), defaultAliasHint(conceptAlias))
            }
        }
    }


    fun validateCorrectTypesInMethodAnnotations(
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

    private fun firstDuplicateAlias(listOfAlias: List<Alias>): Alias? {
        val alreadyUsedAliases: MutableSet<Alias> = mutableSetOf()
        for(alias in listOfAlias) {
            if(alias in alreadyUsedAliases) {
                return alias
            }
            alreadyUsedAliases.add(alias)
        }

        return null // no duplicate
    }

    private fun firstMissingAlias(listOfAlias: Collection<Alias>, listOfMaybeMissingAliases: Collection<Alias>): Alias? {
        return listOfAlias.firstOrNull { alias -> alias !in listOfMaybeMissingAliases }
    }


}