package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.DEFAULT_CONCEPT_ALIAS
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
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoAnnotationOnSuperclasses
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
        val allBuilders = mutableSetOf<KClass<*>>()
        collectBuilderClassesRecursively(allBuilders, builderClass)
        allBuilders.forEach { validateBuilderClassStructureAndMainMethodSyntax(it) }
        allBuilders.forEach { validateBuilderMethodSyntaxDetails(it, schemaAccess) }
    }

    private fun validateBuilderClassStructureAndMainMethodSyntax(builderClass: KClass<*>) {
        validateBuilderClass(builderClass)

        RelevantMethodFetcher.ownMemberFunctions(builderClass).forEach { method ->
            if(!method.hasAnnotation<BuilderMethod>()) {
                throw BuilderMethodSyntaxException(
                    method, "The method is missing " +
                            "the annotation ${BuilderMethod::class.annotationText()}. " +
                            "This annotation must be on every builder method."
                )
            }

            method.valueParameters.forEachIndexed { index, methodParameter ->
                val isLastParameter = index == (method.valueParameters.size - 1)

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
        checkHasNoAnnotationOnSuperclasses(builderClass, BUILDER_CLASS_DESCRIPTION)
    }



    private fun importedAliasFromSuperiorBuilder(builderClass: KClass<*>): Set<String> {
        return builderClass.annotations
            .filterIsInstance<ExpectedAliasFromSuperiorBuilder>()
            .map { it.conceptAlias }
            .toSet()
    }

    private fun validateAndCollectNewAliases(method: KFunction<*>, importedConceptAliases: Set<String>): Set<String> {
        val newConceptAliases: MutableSet<String> = mutableSetOf()

        method.annotations.filterIsInstance<NewConcept>().forEach { newConceptAnnotation ->
            val conceptAlias = newConceptAnnotation.declareConceptAlias
            val conceptClazz = newConceptAnnotation.concept

            if(newConceptAliases.contains(conceptAlias) || importedConceptAliases.contains(conceptAlias)) {
                val allAlreadyUsedConceptAliases = newConceptAliases + importedConceptAliases
                throw BuilderMethodSyntaxException(
                    method, "The alias '$conceptAlias' introduced " +
                            "with the annotation ${NewConcept::class.annotationText()} for concept ${conceptClazz.shortText()} " +
                            "is already used. All already used alias names are ${allAlreadyUsedConceptAliases}. " +
                            "Choose another alias name. ${defaultAliasHint(conceptAlias)}"
                )

            } else {
                newConceptAliases.add(conceptAlias)
            }
        }
        return newConceptAliases
    }


    private fun validateBuilderMethodSyntaxDetails(builderClass: KClass<*>, schemaAccess: SchemaAccess) {
        RelevantMethodFetcher.ownMemberFunctions(builderClass)
            .filter { method -> method.hasAnnotation<BuilderMethod>() }
            .forEach { method ->
                val importedConceptAliases = importedAliasFromSuperiorBuilder(builderClass)
                val newConceptAliases: Set<String> = validateAndCollectNewAliases(method, importedConceptAliases)
                validateNoDuplicateConceptIdentifierDeclaration(method)
                validateUsedAliases(method, importedConceptAliases + newConceptAliases)
                validateNoMissingConceptIdentifierDeclaration(method, newConceptAliases)

                method.valueParameters.forEach { methodParameter ->
                    validateCorrectParameterTypes(method, methodParameter, schemaAccess)
                }
        }
    }

    private fun validateNoMissingConceptIdentifierDeclaration(method: KFunction<*>, newConceptAliases: Set<String>) {
        val conceptAliasesWithConceptIdDeclaration: Set<String> = collectAliasesWithConceptIdentifierDeclaration(method)
        val conceptAliasesWithoutConceptIdDeclaration = newConceptAliases - conceptAliasesWithConceptIdDeclaration

        if(conceptAliasesWithoutConceptIdDeclaration.isNotEmpty()) {
            val defaultAliasHint = conceptAliasesWithoutConceptIdDeclaration
                .map { conceptAlias -> defaultAliasHint(conceptAlias) }
                .firstOrNull { it.isNotBlank() } ?: ""
            throw BuilderMethodSyntaxException(
                method, "The concept with alias " +
                        "$conceptAliasesWithoutConceptIdDeclaration have no corresponding " +
                        "concept identifier declaration. Use the annotation ${SetConceptIdentifierValue::class.annotationText()} " +
                        "or ${SetRandomConceptIdentifierValue::class.annotationText()} to define " +
                        "a concept identifier. $defaultAliasHint"
            )
        }
    }

    private fun collectAliasesWithConceptIdentifierDeclaration(method: KFunction<*>): Set<String> {
        val conceptAliasesWithConceptIdDeclaration: MutableSet<String> = mutableSetOf()

        method.annotations.filterIsInstance<SetRandomConceptIdentifierValue>().forEach { annotation ->
            conceptAliasesWithConceptIdDeclaration.add(annotation.conceptToModifyAlias)
        }

        method.valueParameters.forEach { methodParameter ->
            methodParameter.annotations.filterIsInstance<SetConceptIdentifierValue>().forEach { annotation ->
                conceptAliasesWithConceptIdDeclaration.add(annotation.conceptToModifyAlias)
            }
        }
        return conceptAliasesWithConceptIdDeclaration
    }

    private fun validateNoDuplicateConceptIdentifierDeclaration(method: KFunction<*>) {
        val usedConceptAliasToSetConceptIdentifier: MutableSet<String> = mutableSetOf()
        method.annotations
            .filterIsInstance<SetRandomConceptIdentifierValue>()
            .forEach { autoRandomConceptIdAnnotation ->
            val conceptAlias = autoRandomConceptIdAnnotation.conceptToModifyAlias

            if(usedConceptAliasToSetConceptIdentifier.contains(conceptAlias)) {
                throw BuilderMethodSyntaxException(
                    method, "The alias '$conceptAlias' used " +
                            "with the annotation ${SetRandomConceptIdentifierValue::class.annotationText()} " +
                            "is already used. Choose another alias name. ${defaultAliasHint(conceptAlias)}"
                )
            } else {
                usedConceptAliasToSetConceptIdentifier.add(conceptAlias)
            }
        }

        method.valueParameters.forEach { parameter ->
            parameter.annotations.filterIsInstance<SetConceptIdentifierValue>().forEach { conceptIdValueAnnotation ->
                val conceptAlias = conceptIdValueAnnotation.conceptToModifyAlias
                if(usedConceptAliasToSetConceptIdentifier.contains(conceptAlias)) {
                    throw BuilderMethodSyntaxException(
                        method, "The alias '$conceptAlias' used " +
                                "with the annotation ${SetConceptIdentifierValue::class.annotationText()} " +
                                "is already used. Choose another alias name. ${defaultAliasHint(conceptAlias)}"
                    )
                } else {
                    usedConceptAliasToSetConceptIdentifier.add(conceptAlias)
                }
            }
        }
    }

    private fun validateUsedAliases(method: KFunction<*>, knownConceptAlias: Set<String>) {
        val usedAliasesPerAnnotation = collectAllUsedAliases(method)
        usedAliasesPerAnnotation.forEach { (annotationClazz, conceptAliases) ->
            conceptAliases.forEach { conceptAlias ->
                if(!knownConceptAlias.contains(conceptAlias)) {
                    throw BuilderMethodSyntaxException(
                        method, "The alias '$conceptAlias' used " +
                                "with the annotation ${annotationClazz.annotationText()} " +
                                "is unknown. Choose a known alias name (${knownConceptAlias}) or declare an alias with " +
                                "${NewConcept::class.annotationText()}. ${defaultAliasHint(conceptAlias)}"
                    )
                }
            }
        }
    }

    private class AnnotationAndAliases {
        private val annotationAndAliasMap: MutableMap<KClass<out Annotation>, MutableSet<String>> = mutableMapOf()

        fun add(annotationClass: KClass<out Annotation>, conceptAlias: String) {
            annotationAndAliasMap.getOrPut(annotationClass) { mutableSetOf() }.add(conceptAlias)
        }

        fun forEach(action: (Map.Entry<KClass<out Annotation>, Set<String>>) -> Unit) {
            annotationAndAliasMap.forEach(action)
        }
    }

    private fun collectAllUsedAliases(method: KFunction<*>): AnnotationAndAliases {
        val annotationAndAliases = AnnotationAndAliases()

        method.annotations.filterIsInstance<SetRandomConceptIdentifierValue>().forEach { annotation ->
            annotationAndAliases.add(SetRandomConceptIdentifierValue::class, annotation.conceptToModifyAlias)
        }

        method.annotations.filterIsInstance<SetFixedBooleanFacetValue>().forEach { annotation ->
            annotationAndAliases.add(SetFixedBooleanFacetValue::class, annotation.conceptToModifyAlias)
        }

        method.annotations.filterIsInstance<SetFixedEnumFacetValue>().forEach { annotation ->
            annotationAndAliases.add(SetFixedEnumFacetValue::class, annotation.conceptToModifyAlias)
        }

        method.annotations.filterIsInstance<SetFixedIntFacetValue>().forEach { annotation ->
            annotationAndAliases.add(SetFixedIntFacetValue::class, annotation.conceptToModifyAlias)
        }

        method.annotations.filterIsInstance<SetFixedStringFacetValue>().forEach { annotation ->
            annotationAndAliases.add(SetFixedStringFacetValue::class, annotation.conceptToModifyAlias)
        }

        method.annotations.filterIsInstance<SetAliasConceptIdentifierReferenceFacetValue>().forEach { annotation ->
            annotationAndAliases.add(SetAliasConceptIdentifierReferenceFacetValue::class, annotation.conceptToModifyAlias)
            annotationAndAliases.add(SetAliasConceptIdentifierReferenceFacetValue::class, annotation.referencedConceptAlias)
        }

        method.valueParameters.forEach { methodParameter ->
            methodParameter.annotations.filterIsInstance<SetConceptIdentifierValue>().forEach { annotation ->
                annotationAndAliases.add(SetConceptIdentifierValue::class, annotation.conceptToModifyAlias)
            }
        }

        method.valueParameters.forEach { methodParameter ->
            methodParameter.annotations.filterIsInstance<SetFacetValue>().forEach { annotation ->
                annotationAndAliases.add(SetFacetValue::class, annotation.conceptToModifyAlias)
            }
        }
        return annotationAndAliases
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

    private fun defaultAliasHint(conceptAlias: String): String {
        val showHint = conceptAlias == DEFAULT_CONCEPT_ALIAS
        val hint = "(Hint: The concept alias '${DEFAULT_CONCEPT_ALIAS}' is the default alias and therefore maybe not visible on the annotations)"
        return if(showHint) hint else ""
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