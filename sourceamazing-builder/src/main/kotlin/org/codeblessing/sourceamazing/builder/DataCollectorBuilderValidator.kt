package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.DEFAULT_CONCEPT_ALIAS
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
import org.codeblessing.sourceamazing.builder.typemirror.ExpectedAliasFromSuperiorBuilderAnnotationMirror
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
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ClassTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.MirrorFactory
import org.codeblessing.sourceamazing.schema.typemirror.ParameterMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.TypeHelper
import kotlin.reflect.KClass

object DataCollectorBuilderValidator {
    @Throws(DataCollectorBuilderException::class, DataCollectorBuilderMethodSyntaxException::class)
    fun validateAccessorMethodsOfDataCollector(dataCollectorClass: ClassMirrorInterface) {
        val allBuilders = mutableSetOf<ClassMirrorInterface>()
        collectBuilderClassesRecursively(allBuilders, dataCollectorClass)
        allBuilders.forEach { builderClass -> validateBuilderClassStructure(builderClass) }
        allBuilders.forEach { builderClass -> validateBuilderMethodSyntax(builderClass) }
    }

    private fun validateBuilderClassStructure(builderClass: ClassMirrorInterface) {
        checkHasBuilderAnnotationOnClassAndIsInterface(builderClass)

        builderClass.methods.filter(TypeHelper::isNotFromKotlinAnyClass).forEach { method ->
            if(!method.hasAnnotation(BuilderMethod::class)) {
                throw DataCollectorBuilderMethodSyntaxException(method, "The method is missing " +
                        "the annotation ${BuilderMethod::class.annotationText()}. " +
                        "This annotation must be on every builder method.")
            }

            method.valueParameters.forEachIndexed { index, methodParameter ->
                val isLastParameter = index == (method.valueParameters.size - 1)

                if(methodParameter.hasAnnotation(IgnoreNullFacetValue::class)) {

                    if(methodParameter.hasAnnotation(SetConceptIdentifierValue::class)) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "A parameter setting the" +
                                "concept identifier with ${SetConceptIdentifierValue::class.annotationText()} " +
                                "can not have ${IgnoreNullFacetValue::class.annotationText()} at the same time.")
                    }

                    if(methodParameter.hasAnnotation(InjectBuilder::class)) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "A parameter with ${InjectBuilder::class.annotationText()} " +
                                "can not have ${IgnoreNullFacetValue::class.annotationText()} at the same time.")
                    }
                }

                if(!isLastParameter) {
                    if(methodParameter.hasAnnotation(InjectBuilder::class)) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "Only the last parameter of the method " +
                                "can have the annotation ${InjectBuilder::class.annotationText()}.")
                    }

                    if(!methodParameter.hasAnnotation(SetConceptIdentifierValue::class)
                        && !methodParameter.hasAnnotation(SetFacetValue::class)) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "A parameter of the method " +
                                "is missing one of annotations ${SetConceptIdentifierValue::class.annotationText()} " +
                                "or ${SetFacetValue::class.annotationText()}")
                    }
                } else {
                    if(!methodParameter.hasAnnotation(SetConceptIdentifierValue::class)
                        && !methodParameter.hasAnnotation(SetFacetValue::class)
                        && !methodParameter.hasAnnotation(InjectBuilder::class)) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "The last parameter of the method " +
                                "is missing one of annotations ${SetConceptIdentifierValue::class.annotationText()} " +
                                "or ${SetFacetValue::class.annotationText()} or ${InjectBuilder::class.annotationText()}")
                    }
                }
            }
        }
    }

    private fun importedAliasFromSuperiorBuilder(builderClass: ClassMirrorInterface): Set<String> {
        return builderClass.annotations
            .filterIsInstance<ExpectedAliasFromSuperiorBuilderAnnotationMirror>()
            .map { it.conceptAlias }
            .toSet()
    }

    private fun validateAndCollectNewAliases(method: FunctionMirrorInterface, importedConceptAliases: Set<String>): Set<String> {
        val newConceptAliases: MutableSet<String> = mutableSetOf()

        method.annotations.filterIsInstance<NewConceptAnnotationMirror>().forEach { newConceptAnnotation ->
            val conceptAlias = newConceptAnnotation.declareConceptAlias
            val conceptClazz = newConceptAnnotation.concept.provideMirror()

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


    private fun validateBuilderMethodSyntax(builderClass: ClassMirrorInterface) {
        builderClass.methods
            .filter(TypeHelper::isNotFromKotlinAnyClass)
            .filter { method -> method.hasAnnotation(BuilderMethod::class) }
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

    private fun validateNoMissingConceptIdentifierDeclaration(method: FunctionMirrorInterface, newConceptAliases: Set<String>) {
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

    private fun collectAliasesWithConceptIdentifierDeclaration(method: FunctionMirrorInterface): Set<String> {
        val conceptAliasesWithConceptIdDeclaration: MutableSet<String> = mutableSetOf()

        method.annotations.filterIsInstance<SetRandomConceptIdentifierValueAnnotationMirror>().forEach { annotation ->
            conceptAliasesWithConceptIdDeclaration.add(annotation.conceptToModifyAlias)
        }

        method.valueParameters.forEach { methodParameter ->
            methodParameter.annotations.filterIsInstance<SetConceptIdentifierValueAnnotationMirror>().forEach { annotation ->
                conceptAliasesWithConceptIdDeclaration.add(annotation.conceptToModifyAlias)
            }
        }
        return conceptAliasesWithConceptIdDeclaration
    }

    private fun validateNoDuplicateConceptIdentifierDeclaration(method: FunctionMirrorInterface) {
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

        method.valueParameters.forEach { parameter ->
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

    private fun validateUsedAliases(method: FunctionMirrorInterface, knownConceptAlias: Set<String>) {
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

    private fun collectAllUsedAliases(method: FunctionMirrorInterface): AnnotationAndAliases {
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

        method.valueParameters.forEach { methodParameter ->
            methodParameter.annotations.filterIsInstance<SetConceptIdentifierValueAnnotationMirror>().forEach { annotation ->
                annotationAndAliases.add(SetConceptIdentifierValue::class, annotation.conceptToModifyAlias)
            }
        }

        method.valueParameters.forEach { methodParameter ->
            methodParameter.annotations.filterIsInstance<SetFacetValueAnnotationMirror>().forEach { annotation ->
                annotationAndAliases.add(SetFacetValue::class, annotation.conceptToModifyAlias)
            }
        }
        return annotationAndAliases
    }

    private fun validateCorrectConceptIdentifierType(method: FunctionMirrorInterface, methodParameter: ParameterMirrorInterface) {
        if(methodParameter.hasAnnotation(SetConceptIdentifierValue::class)) {
            when(val typeMirror = methodParameter.type) {
                is ClassTypeMirrorInterface -> {
                    val classMirror = typeMirror.classMirror.provideMirror()
                    if(!classMirror.isClass(MirrorFactory.convertToClassMirror(ConceptIdentifier::class))) {
                        throw DataCollectorBuilderMethodSyntaxException(method, "The parameter of the method " +
                                "to pass a concept identifier (with annotation ${SetConceptIdentifierValue::class.annotationText()}) " +
                                "must be of type '${ConceptIdentifier::class.shortText()}' but was '${classMirror.longText()}'")
                    }
                }
                is FunctionTypeMirrorInterface -> {
                    val functionMirror = typeMirror.functionMirror.provideMirror()
                    throw DataCollectorBuilderMethodSyntaxException(method, "The parameter of the method " +
                            "to pass a concept identifier (with annotation ${SetConceptIdentifierValue::class.annotationText()}) " +
                            "can not be a function but was '${functionMirror.longText()}'")
                }
            }
        }
    }


    private fun collectBuilderClassesRecursively(collectedBuilders: MutableSet<ClassMirrorInterface>, builderClass: ClassMirrorInterface) {
        checkHasBuilderAnnotationOnClassAndIsInterface(builderClass)

        // avoid infinite recursion
        if(!collectedBuilders.contains(builderClass)) {
            collectedBuilders.add(builderClass)
            builderClass.methods.filter(TypeHelper::isNotFromKotlinAnyClass).forEach { method ->
                if(method.hasAnnotation(WithNewBuilder::class)) {
                    val nestedBuilderClass = method.getAnnotationMirror(WithNewBuilderAnnotationMirror::class).builderClass.provideMirror()
                    collectBuilderClassesRecursively(collectedBuilders, nestedBuilderClass)
                }
            }
        }
    }

    private fun checkHasBuilderAnnotationOnClassAndIsInterface(builderClass: ClassMirrorInterface) {
        if(!builderClass.isInterface) {
            throw DataCollectorBuilderException("The builder class must be an interface: ${builderClass.longText()}")
        }

        if(!builderClass.hasAnnotation(Builder::class)) {
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
