package org.codeblessing.sourceamazing.builder

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
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderException
import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.type.KTypeKind
import org.codeblessing.sourceamazing.schema.type.classifierAsClass
import org.codeblessing.sourceamazing.schema.type.classifierAsFunction
import org.codeblessing.sourceamazing.schema.type.isInterface
import org.codeblessing.sourceamazing.schema.type.isFromKotlinAnyClass
import org.codeblessing.sourceamazing.schema.type.typeKind
import org.codeblessing.sourceamazing.schema.typemirror.ClassTypeMirrorInterface
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters

object DataCollectorBuilderValidator {
    @Throws(DataCollectorBuilderException::class, DataCollectorBuilderMethodSyntaxException::class)
    fun validateAccessorMethodsOfDataCollector(builderClass: KClass<*>) {
        val allBuilders = mutableSetOf<KClass<*>>()
        collectBuilderClassesRecursively(allBuilders, builderClass)
        allBuilders.forEach { validateBuilderClassStructure(it) }
        allBuilders.forEach { validateBuilderMethodSyntax(it) }
    }

    private fun validateBuilderClassStructure(builderClass: KClass<*>) {
        checkHasBuilderAnnotationOnClassAndIsInterface(builderClass)

        builderClass.memberFunctions.filterNot { it.isFromKotlinAnyClass() }.forEach { method ->
            val builderMethod = method.findAnnotation<BuilderMethod>()
            if(builderMethod == null) {
                throw DataCollectorBuilderMethodSyntaxException(method, "The method is missing " +
                        "the annotation ${BuilderMethod::class.annotationText()}. " +
                        "This annotation must be on every builder method.")
            }

            method.valueParameters.forEachIndexed { index, methodParameter ->
                val isLastParameter = index == (method.valueParameters.size - 1)

                if(methodParameter.hasAnnotation<IgnoreNullFacetValue>()) {

                    if(methodParameter.hasAnnotation<SetConceptIdentifierValue>()) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "A parameter setting the" +
                                "concept identifier with ${SetConceptIdentifierValue::class.annotationText()} " +
                                "can not have ${IgnoreNullFacetValue::class.annotationText()} at the same time.")
                    }

                    if(methodParameter.hasAnnotation<InjectBuilder>()) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "A parameter with ${InjectBuilder::class.annotationText()} " +
                                "can not have ${IgnoreNullFacetValue::class.annotationText()} at the same time.")
                    }
                }

                if(!isLastParameter) {
                    if(methodParameter.hasAnnotation<InjectBuilder>()) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "Only the last parameter of the method " +
                                "can have the annotation ${InjectBuilder::class.annotationText()}.")
                    }

                    if(!methodParameter.hasAnnotation<SetConceptIdentifierValue>()
                        && !methodParameter.hasAnnotation<SetFacetValue>()) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "A parameter of the method " +
                                "is missing one of annotations ${SetConceptIdentifierValue::class.annotationText()} " +
                                "or ${SetFacetValue::class.annotationText()}")
                    }
                } else {
                    if(!methodParameter.hasAnnotation<SetConceptIdentifierValue>()
                        && !methodParameter.hasAnnotation<SetFacetValue>()
                        && !methodParameter.hasAnnotation<InjectBuilder>()) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "The last parameter of the method " +
                                "is missing one of annotations ${SetConceptIdentifierValue::class.annotationText()} " +
                                "or ${SetFacetValue::class.annotationText()} or ${InjectBuilder::class.annotationText()}")
                    }
                }
            }
        }
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
                throw DataCollectorBuilderMethodSyntaxException(method, "The alias '$conceptAlias' introduced " +
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


    private fun validateBuilderMethodSyntax(builderClass: KClass<*>) {
        builderClass.memberFunctions
            .filterNot { it.isFromKotlinAnyClass() }
            .filter { method -> method.hasAnnotation<BuilderMethod>() }
            .forEach { method ->
                val importedConceptAliases = importedAliasFromSuperiorBuilder(builderClass)
                val newConceptAliases: Set<String> = validateAndCollectNewAliases(method, importedConceptAliases)
                validateNoDuplicateConceptIdentifierDeclaration(method)
                validateUsedAliases(method, importedConceptAliases + newConceptAliases)
                validateNoMissingConceptIdentifierDeclaration(method, newConceptAliases)

                method.valueParameters.forEach { methodParameter ->
                    validateCorrectConceptIdentifierType(method, methodParameter)
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
            throw DataCollectorBuilderMethodSyntaxException(method, "The concept with alias " +
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
                throw DataCollectorBuilderMethodSyntaxException(method, "The alias '$conceptAlias' used " +
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
                    throw DataCollectorBuilderMethodSyntaxException(method, "The alias '$conceptAlias' used " +
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
                    throw DataCollectorBuilderMethodSyntaxException(method, "The alias '$conceptAlias' used " +
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

        fun forEach(action: (Map.Entry<KClass<out Annotation>, Set<String>>) -> Unit): Unit {
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

    private fun validateCorrectConceptIdentifierType(method: KFunction<*>, methodParameter: KParameter) {
        if(methodParameter.hasAnnotation<SetConceptIdentifierValue>()) {
            val methodParamType = methodParameter.type

            when(methodParamType.typeKind()) {
                KTypeKind.KCLASS -> {
                    if(methodParamType.classifierAsClass() != ConceptIdentifier::class) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "The parameter of the method " +
                                "to pass a concept identifier (with annotation ${SetConceptIdentifierValue::class.annotationText()}) " +
                                "must be of type '${ConceptIdentifier::class.shortText()}' but was '${methodParamType.classifierAsClass().longText()}'")

                    }
                }
                KTypeKind.FUNCTION -> {
                    val typeAsFunction = methodParamType.classifierAsFunction()
                    throw DataCollectorBuilderMethodSyntaxException(method, "The parameter of the method " +
                            "to pass a concept identifier (with annotation ${SetConceptIdentifierValue::class.annotationText()}) " +
                            "can not be a function but was '${typeAsFunction}'")
                }
                KTypeKind.OTHER_TYPE, KTypeKind.TYPE_PARAMETER -> {
                    throw DataCollectorBuilderMethodSyntaxException(method, "The parameter of the method " +
                            "to pass a concept identifier (with annotation ${SetConceptIdentifierValue::class.annotationText()}) " +
                            "can only be a class but was '${methodParamType}'")
                }
            }
        }
    }

    private fun collectBuilderClassesRecursively(collectedBuilders: MutableSet<KClass<*>>, builderClass: KClass<*>) {
        checkHasBuilderAnnotationOnClassAndIsInterface(builderClass)

        // avoid infinite recursion
        if(!collectedBuilders.contains(builderClass)) {
            collectedBuilders.add(builderClass)
            builderClass.memberFunctions.filterNot { it.isFromKotlinAnyClass() }.forEach { method ->
                val withNewBuilderAnnotation = method.findAnnotation<WithNewBuilder>()
                if(withNewBuilderAnnotation != null) {
                    collectBuilderClassesRecursively(collectedBuilders, withNewBuilderAnnotation.builderClass)
                }
            }
        }
    }

    private fun checkHasBuilderAnnotationOnClassAndIsInterface(builderClass: KClass<*>) {
        if(!builderClass.isInterface) {
            throw DataCollectorBuilderException("The builder class must be an interface: ${builderClass.longText()}")
        }

        if(!builderClass.hasAnnotation<Builder>()) {
            throw DataCollectorBuilderException("The following class is missing the " +
                    "annotation ${Builder::class.annotationText()}: ${builderClass.longText()}")
        }
    }

    private fun defaultAliasHint(conceptAlias: String): String {
        val showHint = conceptAlias == DEFAULT_CONCEPT_ALIAS
        val hint = "(Hint: The concept alias '$DEFAULT_CONCEPT_ALIAS' is the default alias and therefore maybe not visible on the annotations)"
        return if(showHint) hint else ""
    }
}
