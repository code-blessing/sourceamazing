package org.codeblessing.sourceamazing.builder.interpretation

import kotlin.reflect.KFunction
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.FacetValueAnnotationBaseData
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.FacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.ReferenceFacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.update.DataContext
import org.codeblessing.sourceamazing.schema.api.*

object CommonMethodInterpretationHelper {

    fun extractNewConceptsAsPair(methods: List<KFunction<*>>): List<Pair<Alias, ConceptName>> {
        return methods.flatMap { method ->
            method.annotations.filterIsInstance<NewConcept>().map {
                Pair(it.declareConceptAlias.toAlias(), it.concept.toConceptName())
            }
        }
    }

    fun extractAliasesToSetRandomConceptIdentifierValueIncludingDuplicates(methods: List<KFunction<*>>): List<Alias> {
        return methods.flatMap { method ->
            method.annotations.filterIsInstance<SetRandomConceptIdentifierValue>().map {
                it.conceptToModifyAlias.toAlias()
            }
        }
    }

    fun extractFixedFacetValues(
        method: KFunction<*>,
        methodLocation: MethodLocation,
        aliases: Map<Alias, ConceptName>,
        schemaAccess: SchemaAccess,
        dataContext: DataContext?,
    ): List<FacetValueAnnotationContent> {
        val facetValues: MutableList<FacetValueAnnotationContent> = mutableListOf()

        method.annotations
            .filterIsInstance<SetFixedBooleanFacetValue>()
            .map { annotation ->
                FacetValueAnnotationContent(
                    base =
                        FacetValueAnnotationBaseData(
                            methodLocation = methodLocation,
                            alias = annotation.conceptToModifyAlias.toAlias(),
                            facetName = annotation.facetToModify.toFacetName(),
                            facetModificationRule = annotation.facetModificationRule,
                            annotation = annotation,
                            ignoreNullValue = false,
                            type = null,
                            typeClass = Boolean::class,
                        ),
                    expectedFacetType = FacetType.BOOLEAN,
                    value = annotation.value,
                )
            }
            .forEach(facetValues::add)

        method.annotations
            .filterIsInstance<SetFixedEnumFacetValue>()
            .map { annotation ->
                val enumConceptAlias = annotation.conceptToModifyAlias.toAlias()
                val facetName = annotation.facetToModify.toFacetName()
                val facetSchema = resolveFacetSchema(aliases, enumConceptAlias, facetName, schemaAccess)
                val enumValue = facetSchema?.let { enumValueByString(it, annotation.value) }

                FacetValueAnnotationContent(
                    base =
                        FacetValueAnnotationBaseData(
                            methodLocation = methodLocation,
                            alias = enumConceptAlias,
                            facetName = facetName,
                            facetModificationRule = annotation.facetModificationRule,
                            annotation = annotation,
                            ignoreNullValue = false,
                            type = null,
                            typeClass = enumValue?.let { it::class },
                        ),
                    expectedFacetType = FacetType.TEXT_ENUMERATION,
                    value = enumValue,
                )
            }
            .forEach(facetValues::add)

        method.annotations
            .filterIsInstance<SetFixedIntFacetValue>()
            .map { annotation ->
                FacetValueAnnotationContent(
                    base =
                        FacetValueAnnotationBaseData(
                            methodLocation = methodLocation,
                            alias = annotation.conceptToModifyAlias.toAlias(),
                            facetName = annotation.facetToModify.toFacetName(),
                            facetModificationRule = annotation.facetModificationRule,
                            annotation = annotation,
                            ignoreNullValue = false,
                            type = null,
                            typeClass = Int::class,
                        ),
                    expectedFacetType = FacetType.NUMBER,
                    value = annotation.value,
                )
            }
            .forEach(facetValues::add)

        method.annotations
            .filterIsInstance<SetFixedStringFacetValue>()
            .map { annotation ->
                FacetValueAnnotationContent(
                    base =
                        FacetValueAnnotationBaseData(
                            methodLocation = methodLocation,
                            alias = annotation.conceptToModifyAlias.toAlias(),
                            facetName = annotation.facetToModify.toFacetName(),
                            facetModificationRule = annotation.facetModificationRule,
                            annotation = annotation,
                            ignoreNullValue = false,
                            type = null,
                            typeClass = String::class,
                        ),
                    expectedFacetType = FacetType.TEXT,
                    value = annotation.value,
                )
            }
            .forEach(facetValues::add)

        method.annotations
            .filterIsInstance<SetAliasConceptIdentifierReferenceFacetValue>()
            .map { annotation ->
                val referencedAlias = annotation.referencedConceptAlias.toAlias()
                val referenceConceptId = dataContext?.conceptIdByAlias(referencedAlias)

                ReferenceFacetValueAnnotationContent(
                    base =
                        FacetValueAnnotationBaseData(
                            methodLocation = methodLocation,
                            alias = annotation.conceptToModifyAlias.toAlias(),
                            facetName = annotation.facetToModify.toFacetName(),
                            facetModificationRule = annotation.facetModificationRule,
                            annotation = annotation,
                            ignoreNullValue = false,
                            type = null,
                            typeClass = ConceptIdentifier::class,
                        ),
                    referencedAlias = referencedAlias,
                    value = referenceConceptId,
                )
            }
            .forEach(facetValues::add)
        return facetValues
    }

    private fun resolveFacetSchema(
        aliases: Map<Alias, ConceptName>,
        conceptAlias: Alias,
        facetName: FacetName,
        schemaAccess: SchemaAccess,
    ): FacetSchema? {
        val conceptName = aliases[conceptAlias] ?: return null
        return schemaAccess.facetByFacetName(conceptName, facetName)
    }

    private fun enumValueByString(facetSchema: FacetSchema, enumValueString: String): Enum<*>? {
        // TODO maybe use org.codeblessing.sourceamazing.utils.enumeration.EnumUtil
        // TODO maybe think about putting plain strings into the model and let it validate by schema/concept resolver
        return if (facetSchema is EnumFacetSchema) {
            facetSchema.enumerationValues.firstOrNull { it.name == enumValueString }
        } else {
            null
        }
    }

    fun castConceptIdentifier(value: Any): ConceptIdentifier {
        return when (value) {
            is ConceptIdentifier -> value
            is String -> ConceptIdentifier.of(value)
            else ->
                throw IllegalArgumentException(
                    "Concept identifier must be a ${String::class} or a ${ConceptIdentifier::class}."
                )
        }
    }
}
