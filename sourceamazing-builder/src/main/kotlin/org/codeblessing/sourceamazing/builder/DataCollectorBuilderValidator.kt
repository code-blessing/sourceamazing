package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderException
import org.codeblessing.sourceamazing.builder.exceptions.DataCollectorBuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.typemirror.BuilderAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.BuilderMethodAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.ExpectedAliasFromSuperiorBuilderAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.IgnoreNullFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.InjectBuilderAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.NewConceptAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetAliasConceptIdentifierReferenceFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetConceptIdentifierValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFixedBooleanFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFixedEnumFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFixedIntFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFixedStringFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetRandomConceptIdentifierValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.WithNewBuilderAnnotationMirror
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.MethodMirror
import org.codeblessing.sourceamazing.schema.typemirror.ParameterMirror
import org.codeblessing.sourceamazing.schema.util.AnnotationUtil
import kotlin.reflect.KClass

object DataCollectorBuilderValidator {
    @Throws(DataCollectorBuilderException::class, DataCollectorBuilderMethodSyntaxException::class)
    fun validateAccessorMethodsOfDataCollector(dataCollectorClass: ClassMirror) {
        val allBuilders = mutableSetOf<ClassMirror>()
        collectBuilderClassesRecursively(allBuilders, dataCollectorClass)
        allBuilders.forEach { builderClass -> validateBuilderClassStructure(builderClass) }
        allBuilders.forEach { builderClass -> validateBuilderMethodSyntax(builderClass) }
    }

    private fun validateBuilderClassStructure(builderClass: ClassMirror) {
        checkHasBuilderAnnotationOnClassAndIsInterface(builderClass)

        builderClass.methods.forEach { method ->
            if(!method.hasAnnotationMirror(BuilderMethodAnnotationMirror::class)) {
                throw DataCollectorBuilderMethodSyntaxException(method, "The method is missing " +
                        "the annotation ${BuilderMethod::class.annotationText()}. " +
                        "This annotation must be on every builder method.")
            }

            method.parameters.forEachIndexed { index, methodParameter ->
                val isLastParameter = index == (method.parameters.size - 1)

                if(methodParameter.hasAnnotationMirror(IgnoreNullFacetValueAnnotationMirror::class)) {

                    if(methodParameter.hasAnnotationMirror(SetConceptIdentifierValueAnnotationMirror::class)) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "A parameter setting the" +
                                "concept identifier with ${SetConceptIdentifierValue::class.annotationText()} " +
                                "can not have ${IgnoreNullFacetValue::class.annotationText()} at the same time.")
                    }

                    if(methodParameter.hasAnnotationMirror(InjectBuilderAnnotationMirror::class)) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "A parameter with ${InjectBuilder::class.annotationText()} " +
                                "can not have ${IgnoreNullFacetValue::class.annotationText()} at the same time.")
                    }
                }

                if(!isLastParameter) {
                    if(methodParameter.hasAnnotationMirror(InjectBuilderAnnotationMirror::class)) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "Only the last parameter of the method " +
                                "can have the annotation ${InjectBuilder::class.annotationText()}.")
                    }

                    if(!methodParameter.hasAnnotationMirror(SetConceptIdentifierValueAnnotationMirror::class)
                        && !methodParameter.hasAnnotationMirror(SetFacetValueAnnotationMirror::class)) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "A parameter of the method " +
                                "is missing one of annotations ${SetConceptIdentifierValue::class.annotationText()} " +
                                "or ${SetFacetValue::class.annotationText()}")
                    }
                } else {
                    if(!methodParameter.hasAnnotationMirror(SetConceptIdentifierValueAnnotationMirror::class)
                        && !methodParameter.hasAnnotationMirror(SetFacetValueAnnotationMirror::class)
                        && !methodParameter.hasAnnotationMirror(InjectBuilderAnnotationMirror::class)) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "The last parameter of the method " +
                                "is missing one of annotations ${SetConceptIdentifierValue::class.annotationText()} " +
                                "or ${SetFacetValue::class.annotationText()} or ${InjectBuilder::class.annotationText()}")
                    }
                }
            }
        }
    }

    private fun importedAliasFromSuperiorBuilder(builderClass: ClassMirror): Set<String> {
        return builderClass.annotations
            .filterIsInstance<ExpectedAliasFromSuperiorBuilderAnnotationMirror>()
            .map { it.conceptAlias }
            .toSet()
    }

    private fun validateAndCollectNewAliases(method: MethodMirror, importedConceptAliases: Set<String>): Set<String> {
        val newConceptAliases: MutableSet<String> = mutableSetOf()

        method.annotations.filterIsInstance<NewConceptAnnotationMirror>().forEach { newConceptAnnotation ->
            val conceptAlias = newConceptAnnotation.declareConceptAlias
            val conceptClazz = newConceptAnnotation.concept.provideClassMirror()

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


    private fun validateBuilderMethodSyntax(builderClass: ClassMirror) {
        builderClass.methods
            .filter { method -> method.hasAnnotationMirror(BuilderMethodAnnotationMirror::class) }
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

    private fun validateNoMissingConceptIdentifierDeclaration(method: MethodMirror, newConceptAliases: Set<String>) {
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

    private fun collectAliasesWithConceptIdentifierDeclaration(method: MethodMirror): Set<String> {
        val conceptAliasesWithConceptIdDeclaration: MutableSet<String> = mutableSetOf()

        method.annotations.filterIsInstance<SetRandomConceptIdentifierValueAnnotationMirror>().forEach { annotation ->
            conceptAliasesWithConceptIdDeclaration.add(annotation.conceptToModifyAlias)
        }

        method.parameters.forEach { methodParameter ->
            method.annotations.filterIsInstance<SetConceptIdentifierValueAnnotationMirror>().forEach { annotation ->
                conceptAliasesWithConceptIdDeclaration.add(annotation.conceptToModifyAlias)
            }
        }
        return conceptAliasesWithConceptIdDeclaration
    }

    private fun validateNoDuplicateConceptIdentifierDeclaration(method: MethodMirror) {
        val usedConceptAliasToSetConceptIdentifier: MutableSet<String> = mutableSetOf()
        method.annotations.filterIsInstance<SetRandomConceptIdentifierValueAnnotationMirror>().forEach { autoRandomConceptIdAnnotation ->
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

        method.parameters.forEach { parameter ->
            parameter.annotations.filterIsInstance<SetConceptIdentifierValueAnnotationMirror>().forEach { conceptIdValueAnnotation ->
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

    private fun validateUsedAliases(method: MethodMirror, knownConceptAlias: Set<String>) {
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

    private fun collectAllUsedAliases(method: MethodMirror): AnnotationAndAliases {
        val annotationAndAliases = AnnotationAndAliases()

        method.annotations.filterIsInstance<SetRandomConceptIdentifierValueAnnotationMirror>().forEach { annotation ->
            annotationAndAliases.add(SetRandomConceptIdentifierValue::class, annotation.conceptToModifyAlias)
        }

        method.annotations.filterIsInstance<SetFixedBooleanFacetValueAnnotationMirror>().forEach { annotation ->
            annotationAndAliases.add(SetFixedBooleanFacetValue::class, annotation.conceptToModifyAlias)
        }

        method.annotations.filterIsInstance<SetFixedEnumFacetValueAnnotationMirror>().forEach { annotation ->
            annotationAndAliases.add(SetFixedEnumFacetValue::class, annotation.conceptToModifyAlias)
        }

        method.annotations.filterIsInstance<SetFixedIntFacetValueAnnotationMirror>().forEach { annotation ->
            annotationAndAliases.add(SetFixedIntFacetValue::class, annotation.conceptToModifyAlias)
        }

        method.annotations.filterIsInstance<SetFixedStringFacetValueAnnotationMirror>().forEach { annotation ->
            annotationAndAliases.add(SetFixedStringFacetValue::class, annotation.conceptToModifyAlias)
        }

        method.annotations.filterIsInstance<SetAliasConceptIdentifierReferenceFacetValueAnnotationMirror>().forEach { annotation ->
            annotationAndAliases.add(SetAliasConceptIdentifierReferenceFacetValue::class, annotation.conceptToModifyAlias)
            annotationAndAliases.add(SetAliasConceptIdentifierReferenceFacetValue::class, annotation.referencedConceptAlias)
        }

        method.parameters.forEach { methodParameter ->
            method.annotations.filterIsInstance<SetConceptIdentifierValueAnnotationMirror>().forEach { annotation ->
                annotationAndAliases.add(SetConceptIdentifierValue::class, annotation.conceptToModifyAlias)
            }
        }

        method.parameters.forEach { methodParameter ->
            method.annotations.filterIsInstance<SetFacetValueAnnotationMirror>().forEach { annotation ->
                annotationAndAliases.add(SetFacetValue::class, annotation.conceptToModifyAlias)
            }
        }
        return annotationAndAliases
    }

    private fun validateCorrectConceptIdentifierType(method: MethodMirror, methodParameter: ParameterMirror) {
        if(methodParameter.hasAnnotationMirror(SetConceptIdentifierValueAnnotationMirror::class)) {
            val classMirror = methodParameter.type.classMirror.provideClassMirror()
            if(!classMirror.isClass(ConceptIdentifier::class)) {
                throw DataCollectorBuilderMethodSyntaxException(method, "The parameter of the method " +
                        "to pass a concept identifier (with annotation ${SetConceptIdentifierValue::class.annotationText()}) " +
                        "must be of type '${ConceptIdentifier::class.shortText()}' but was '${classMirror.longText()}'")
            }
        }
    }


    private fun collectBuilderClassesRecursively(collectedBuilders: MutableSet<ClassMirror>, builderClass: ClassMirror) {
        checkHasBuilderAnnotationOnClassAndIsInterface(builderClass)

        // avoid infinite recursion
        if(!collectedBuilders.contains(builderClass)) {
            collectedBuilders.add(builderClass)
            builderClass.methods.forEach { method ->
                if(method.hasAnnotationMirror(WithNewBuilderAnnotationMirror::class)) {
                    val nestedBuilderClass = method.getAnnotationMirror(WithNewBuilderAnnotationMirror::class).builderClass.provideClassMirror()
                    collectBuilderClassesRecursively(collectedBuilders, nestedBuilderClass)
                }
            }
        }
    }

    private fun checkHasBuilderAnnotationOnClassAndIsInterface(builderClass: ClassMirror) {
        if(!builderClass.isInterface) {
            throw DataCollectorBuilderException("The builder class must be an interface: ${builderClass.longText()}")
        }

        if(!builderClass.hasAnnotationMirror(BuilderAnnotationMirror::class)) {
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
