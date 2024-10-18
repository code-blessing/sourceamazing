package org.codeblessing.sourceamazing.builder.validation

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
import org.codeblessing.sourceamazing.schema.toFacetName
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

object BuilderAliasValidator {

    fun validateBuilderAlias(builderClass: KClass<*>) {
        RelevantMethodFetcher.ownMemberFunctions(builderClass).forEach { method ->
            val importedConceptAliases = importedAliasFromSuperiorBuilder(builderClass)
            val newConceptAliases: Set<String> = validateAndCollectNewAliases(method, importedConceptAliases)
            validateNoDuplicateConceptIdentifierDeclaration(method)
            validateUsedAliases(method, importedConceptAliases + newConceptAliases)
            validateNoMissingConceptIdentifierDeclaration(method, newConceptAliases)
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

    private fun defaultAliasHint(conceptAlias: String): String {
        val showHint = conceptAlias == DEFAULT_CONCEPT_ALIAS
        val hint = "(Hint: The concept alias '${DEFAULT_CONCEPT_ALIAS}' is the default alias and therefore maybe not visible on the annotations)"
        return if(showHint) hint else ""
    }
}