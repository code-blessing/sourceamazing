package org.codeblessing.sourceamazing.engine.process.datacollection.builder

import org.codeblessing.sourceamazing.api.process.datacollection.builder.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.engine.process.datacollection.builder.exceptions.DataCollectorBuilderException
import org.codeblessing.sourceamazing.engine.process.datacollection.builder.exceptions.DataCollectorBuilderMethodSyntaxException
import org.codeblessing.sourceamazing.engine.process.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.engine.process.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.engine.process.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.engine.process.util.AnnotationUtil
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.reflect.KClass

object DataCollectorBuilderValidator {
    @Throws(DataCollectorBuilderException::class, DataCollectorBuilderMethodSyntaxException::class)
    fun validateAccessorMethodsOfDataCollector(dataCollectorClass: KClass<*>) {
        val allBuilders = mutableSetOf<KClass<*>>()
        collectBuilderClassesRecursively(allBuilders, dataCollectorClass)
        allBuilders.forEach { builderClass -> validateBuilderClassStructure(builderClass) }
        allBuilders.forEach { builderClass -> validateBuilderMethodSyntax(builderClass) }
    }

    private fun validateBuilderClassStructure(builderClass: KClass<*>) {
        checkHasBuilderAnnotationOnClassAndIsInterface(builderClass)

        builderClass.java.methods.forEach { method ->
            if(!AnnotationUtil.hasAnnotation(method, BuilderMethod::class)) {
                throw DataCollectorBuilderMethodSyntaxException(method, "The method is missing " +
                        "the annotation ${BuilderMethod::class.annotationText()}. " +
                        "This annotation must be on every builder method.")
            }

            method.parameters.forEachIndexed { index, methodParameter ->
                val isLastParameter = index == (method.parameterCount - 1)

                if(!isLastParameter) {
                    if(AnnotationUtil.hasAnnotation(methodParameter, InjectBuilder::class)) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "Only the last parameter of the method " +
                                "can have the annotation ${InjectBuilder::class.annotationText()}.")
                    }

                    if(!AnnotationUtil.hasAnnotation(methodParameter, SetConceptIdentifierValue::class)
                        && !AnnotationUtil.hasAnnotation(methodParameter, SetFacetValue::class)) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "A parameter of the method " +
                                "is missing one of annotations ${SetConceptIdentifierValue::class.annotationText()} " +
                                "or ${SetFacetValue::class.annotationText()}")
                    }
                } else {
                    if(!AnnotationUtil.hasAnnotation(methodParameter, SetConceptIdentifierValue::class)
                        && !AnnotationUtil.hasAnnotation(methodParameter, SetFacetValue::class)
                        && !AnnotationUtil.hasAnnotation(methodParameter, InjectBuilder::class)) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "The last parameter of the method " +
                                "is missing one of annotations ${SetConceptIdentifierValue::class.annotationText()} " +
                                "or ${SetFacetValue::class.annotationText()} or ${InjectBuilder::class.annotationText()}")
                    }
                }
            }
        }
    }

    private fun importedAliasFromSuperiorBuilder(builderClass: KClass<*>): Set<String> {
        return AnnotationUtil
            .getAnnotations(builderClass, ExpectedAliasFromSuperiorBuilder::class)
            .map { it.conceptAlias }
            .toSet()

    }

    private fun validateAndCollectNewAliases(method: Method, importedConceptAliases: Set<String>): Set<String> {
        val newConceptAliases: MutableSet<String> = mutableSetOf()

        AnnotationUtil.getAnnotations(method, NewConcept::class).forEach { newConceptAnnotation ->
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
        builderClass.java.methods
            .filter { AnnotationUtil.hasAnnotation(it, BuilderMethod::class) }
            .forEach { method ->
                val importedConceptAliases = importedAliasFromSuperiorBuilder(builderClass)
                val newConceptAliases: Set<String> = validateAndCollectNewAliases(method, importedConceptAliases)
                validateNoDuplicateConceptIdentifierDeclaration(method)
                validateUsedAliases(method, importedConceptAliases + newConceptAliases)
                validateNoMissingConceptIdentifierDeclaration(method, newConceptAliases)

                method.parameters.forEach { methodParameter ->
                    validateCorrectConceptIdentifierType(method, methodParameter)
                }
        }
    }

    private fun validateNoMissingConceptIdentifierDeclaration(method: Method, newConceptAliases: Set<String>) {
        val conceptAliasesWithConceptIdDeclaration: Set<String> = collectAliasesWithConceptIdentifierDeclaration(method)
        val conceptAliasesWithoutConceptIdDeclaration = newConceptAliases - conceptAliasesWithConceptIdDeclaration

        if(conceptAliasesWithoutConceptIdDeclaration.isNotEmpty()) {
            val defaultAliasHint = conceptAliasesWithoutConceptIdDeclaration
                .map { conceptAlias -> defaultAliasHint(conceptAlias) }
                .firstOrNull { it.isNotBlank() } ?: ""
            throw DataCollectorBuilderMethodSyntaxException(method, "The concept with alias " +
                    "$conceptAliasesWithoutConceptIdDeclaration have no corresponding " +
                    "concept identifier declaration. Use the annotation ${SetConceptIdentifierValue::class.annotationText()} " +
                    "or ${SetRandomConceptIdentifier::class.annotationText()} to define " +
                    "a concept identifier. $defaultAliasHint"
            )
        }
    }

    private fun collectAliasesWithConceptIdentifierDeclaration(method: Method): Set<String> {
        val conceptAliasesWithConceptIdDeclaration: MutableSet<String> = mutableSetOf()

        AnnotationUtil.getAnnotations(method, SetRandomConceptIdentifier::class).forEach { annotation ->
            conceptAliasesWithConceptIdDeclaration.add(annotation.conceptToModifyAlias)
        }

        method.parameters.forEach { methodParameter ->
            AnnotationUtil.getAnnotations(methodParameter, SetConceptIdentifierValue::class).forEach { annotation ->
                conceptAliasesWithConceptIdDeclaration.add(annotation.conceptToModifyAlias)
            }
        }
        return conceptAliasesWithConceptIdDeclaration
    }

    private fun validateNoDuplicateConceptIdentifierDeclaration(method: Method) {
        val usedConceptAliasToSetConceptIdentifier: MutableSet<String> = mutableSetOf()
        AnnotationUtil.getAnnotations(method, SetRandomConceptIdentifier::class).forEach { autoRandomConceptIdAnnotation ->
            val conceptAlias = autoRandomConceptIdAnnotation.conceptToModifyAlias

            if(usedConceptAliasToSetConceptIdentifier.contains(conceptAlias)) {
                throw DataCollectorBuilderMethodSyntaxException(method, "The alias '$conceptAlias' used " +
                        "with the annotation ${SetRandomConceptIdentifier::class.annotationText()} " +
                        "is already used. Choose another alias name. ${defaultAliasHint(conceptAlias)}"
                )
            } else {
                usedConceptAliasToSetConceptIdentifier.add(conceptAlias)
            }
        }

        method.parameters.forEach { parameter ->
            AnnotationUtil.getAnnotations(parameter, SetConceptIdentifierValue::class).forEach { conceptIdValueAnnotation ->
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

    private fun validateUsedAliases(method: Method, knownConceptAlias: Set<String>) {
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

    private fun collectAllUsedAliases(method: Method): AnnotationAndAliases {
        val annotationAndAliases = AnnotationAndAliases()

        AnnotationUtil.getAnnotations(method, SetRandomConceptIdentifier::class).forEach { annotation ->
            annotationAndAliases.add(SetRandomConceptIdentifier::class, annotation.conceptToModifyAlias)
        }

        AnnotationUtil.getAnnotations(method, SetFixedBooleanFacetValue::class).forEach { annotation ->
            annotationAndAliases.add(SetFixedBooleanFacetValue::class, annotation.conceptToModifyAlias)
        }

        AnnotationUtil.getAnnotations(method, SetFixedEnumFacetValue::class).forEach { annotation ->
            annotationAndAliases.add(SetFixedEnumFacetValue::class, annotation.conceptToModifyAlias)
        }

        AnnotationUtil.getAnnotations(method, SetFixedIntFacetValue::class).forEach { annotation ->
            annotationAndAliases.add(SetFixedIntFacetValue::class, annotation.conceptToModifyAlias)
        }

        AnnotationUtil.getAnnotations(method, SetFixedStringFacetValue::class).forEach { annotation ->
            annotationAndAliases.add(SetFixedStringFacetValue::class, annotation.conceptToModifyAlias)
        }

        AnnotationUtil.getAnnotations(method, SetAliasConceptIdentifierReferenceFacetValue::class).forEach { annotation ->
            annotationAndAliases.add(SetAliasConceptIdentifierReferenceFacetValue::class, annotation.conceptToModifyAlias)
            annotationAndAliases.add(SetAliasConceptIdentifierReferenceFacetValue::class, annotation.referencedConceptAlias)
        }

        method.parameters.forEach { methodParameter ->
            AnnotationUtil.getAnnotations(methodParameter, SetConceptIdentifierValue::class).forEach { annotation ->
                annotationAndAliases.add(SetConceptIdentifierValue::class, annotation.conceptToModifyAlias)
            }
        }

        method.parameters.forEach { methodParameter ->
            AnnotationUtil.getAnnotations(methodParameter, SetFacetValue::class).forEach { annotation ->
                annotationAndAliases.add(SetFacetValue::class, annotation.conceptToModifyAlias)
            }
        }
        return annotationAndAliases
    }

    private fun validateCorrectConceptIdentifierType(method: Method, methodParameter: Parameter) {
        if(AnnotationUtil.hasAnnotation(methodParameter, SetConceptIdentifierValue::class)) {
            if(methodParameter.type != ConceptIdentifier::class.java) {
                throw DataCollectorBuilderMethodSyntaxException(method, "The parameter of the method " +
                        "to pass a concept identifier (with annotation ${SetConceptIdentifierValue::class.annotationText()}) " +
                        "must be of type '${ConceptIdentifier::class.shortText()}' but was '${methodParameter.type.longText()}'")
            }
        }
    }


    private fun collectBuilderClassesRecursively(collectedBuilders: MutableSet<KClass<*>>, builderClass: KClass<*>) {
        checkHasBuilderAnnotationOnClassAndIsInterface(builderClass)

        // avoid infinite recursion
        if(!collectedBuilders.contains(builderClass)) {
            collectedBuilders.add(builderClass)
            builderClass.java.methods.forEach { method ->
                if(AnnotationUtil.hasAnnotation(method, WithNewBuilder::class)) {
                    val nestedBuilderClass = AnnotationUtil.getAnnotation(method, WithNewBuilder::class).builderClass
                    collectBuilderClassesRecursively(collectedBuilders, nestedBuilderClass)
                }

            }
        }
    }

    private fun checkHasBuilderAnnotationOnClassAndIsInterface(builderClass: KClass<*>) {
        if(!builderClass.java.isInterface) {
            throw DataCollectorBuilderException("The builder class must be an interface: ${builderClass.longText()}")
        }

        if(!AnnotationUtil.hasAnnotation(builderClass, Builder::class)) {
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
