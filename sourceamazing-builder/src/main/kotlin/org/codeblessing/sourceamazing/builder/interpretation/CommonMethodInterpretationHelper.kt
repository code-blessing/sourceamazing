package org.codeblessing.sourceamazing.builder.interpretation

import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedBooleanFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedEnumFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedIntFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedStringFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.EnumFacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.FacetValueAnnotationBaseData
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.FacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.ReferenceFacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.update.DataContext
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.toConceptName
import org.codeblessing.sourceamazing.schema.toFacetName
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KFunction

object CommonMethodInterpretationHelper {

    fun extractNewConceptsAsPair(methods: List<KFunction<*>>): List<Pair<Alias, ConceptName>> {
        return methods.flatMap { method ->
            method.annotations
                .filterIsInstance<NewConcept>()
                .map { Pair(it.declareConceptAlias.toAlias(), it.concept.toConceptName()) }
        }
    }

    fun extractAliasesToSetRandomConceptIdentifierValueIncludingDuplicates(methods: List<KFunction<*>>): List<Alias> {
        return methods.flatMap { method ->
            method.annotations
                .filterIsInstance<SetRandomConceptIdentifierValue>()
                .map { it.conceptToModifyAlias.toAlias()}
        }
    }

    fun extractAliasesToSetConceptIdentifierValueAliasesIncludingDuplicates(methodOrParams: List<KAnnotatedElement>): List<Alias> {
        return methodOrParams
            .flatMap { parameter -> parameter.annotations.filterIsInstance<SetConceptIdentifierValue>() }
            .map { it.conceptToModifyAlias.toAlias()}
    }

    fun extractFixedFacetValues(method: KFunction<*>, methodLocation: MethodLocation, schemaAccess: SchemaAccess, dataContext: DataContext?): List<FacetValueAnnotationContent> {
        val facetValues: MutableList<FacetValueAnnotationContent> = mutableListOf()

        method.annotations.filterIsInstance<SetFixedBooleanFacetValue>().map { annotation ->
                FacetValueAnnotationContent(
                    base = FacetValueAnnotationBaseData(
                        methodLocation = methodLocation,
                        alias = annotation.conceptToModifyAlias.toAlias(),
                        facetName = annotation.facetToModify.toFacetName(),
                        facetModificationRule = annotation.facetModificationRule,
                        annotation = annotation,
                        ignoreNullValue = false,
                        type = null,
                        typeClass = annotation.value::class,
                    ),
                    expectedFacetType = FacetType.BOOLEAN,
                    value = annotation.value
                )
        }.forEach(facetValues::add)

        method.annotations.filterIsInstance<SetFixedEnumFacetValue>().map { annotation ->
            val facetName = annotation.facetToModify.toFacetName()
            val enumValue = enumValueByString(annotation.value, facetName, schemaAccess)

            EnumFacetValueAnnotationContent(
                base = FacetValueAnnotationBaseData(
                    methodLocation = methodLocation,
                    alias = annotation.conceptToModifyAlias.toAlias(),
                    facetName = facetName,
                    facetModificationRule = annotation.facetModificationRule,
                    annotation = annotation,
                    ignoreNullValue = false,
                    type = null,
                    typeClass = enumValue?.let { it::class }

                ),
                fixedEnumValue = annotation.value,
                enumValue = enumValue
            )
        }.forEach(facetValues::add)

        method.annotations.filterIsInstance<SetFixedIntFacetValue>().map { annotation ->
            FacetValueAnnotationContent(
                base = FacetValueAnnotationBaseData(
                    methodLocation = methodLocation,
                    alias = annotation.conceptToModifyAlias.toAlias(),
                    facetName = annotation.facetToModify.toFacetName(),
                    facetModificationRule = annotation.facetModificationRule,
                    annotation = annotation,
                    ignoreNullValue = false,
                    type = null,
                    typeClass = annotation.value::class,
                ),
                expectedFacetType = FacetType.NUMBER,
                value = annotation.value,
            )
        }.forEach(facetValues::add)

        method.annotations.filterIsInstance<SetFixedStringFacetValue>().map { annotation ->
            FacetValueAnnotationContent(
                base = FacetValueAnnotationBaseData(
                    methodLocation = methodLocation,
                    alias = annotation.conceptToModifyAlias.toAlias(),
                    facetName = annotation.facetToModify.toFacetName(),
                    facetModificationRule = annotation.facetModificationRule,
                    annotation = annotation,
                    ignoreNullValue = false,
                    type = null,
                    typeClass = annotation.value::class,
                ),
                expectedFacetType = FacetType.TEXT,
                value = annotation.value,
            )
        }.forEach(facetValues::add)

        method.annotations.filterIsInstance<SetAliasConceptIdentifierReferenceFacetValue>().map { annotation ->
            val referencedAlias = annotation.referencedConceptAlias.toAlias()
            val referenceConceptId = dataContext?.conceptIdByAlias(referencedAlias)

            ReferenceFacetValueAnnotationContent(
                base = FacetValueAnnotationBaseData(
                    methodLocation = methodLocation,
                    alias = annotation.conceptToModifyAlias.toAlias(),
                    facetName = annotation.facetToModify.toFacetName(),
                    facetModificationRule = annotation.facetModificationRule,
                    annotation = annotation,
                    ignoreNullValue = false,
                    type = null,
                    typeClass = referenceConceptId?.let { it::class } ?: ConceptIdentifier::class,
                ),
                referencedAlias = referencedAlias,
                value = referenceConceptId
            )
        }.forEach(facetValues::add)
        return facetValues
    }

    private fun enumValueByString(enumValueString: String, facetName: FacetName, schemaAccess: SchemaAccess):Enum<*>? {
        val facetSchema = schemaAccess.facetByFacetName(facetName)
        return facetSchema?.enumerationValues?.firstOrNull {
            it.name == enumValueString
        }
    }

    fun castConceptIdentifier(value: Any): ConceptIdentifier {
        return when(value) {
            is ConceptIdentifier -> value
            is String -> ConceptIdentifier.of(value)
            else -> throw IllegalArgumentException("Concept identifier must be a ${String::class} or a ${ConceptIdentifier::class}.")
        }
    }

}