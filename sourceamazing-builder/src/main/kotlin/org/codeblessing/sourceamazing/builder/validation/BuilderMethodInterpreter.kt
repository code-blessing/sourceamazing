package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedBooleanFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedEnumFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedIntFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedStringFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.facetvalue.ConceptIdentifierAnnotationData
import org.codeblessing.sourceamazing.builder.facetvalue.EnumFacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.facetvalue.FacetValueAnnotationBaseData
import org.codeblessing.sourceamazing.builder.facetvalue.FacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.facetvalue.ReferenceFacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.update.DataContext
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.toConceptName
import org.codeblessing.sourceamazing.schema.toFacetName
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.valueParameters

class BuilderMethodInterpreter(
    val schemaAccess: SchemaAccess,
    val builderClassInterpreter: BuilderClassInterpreter,
    val method: KFunction<*>,
) {

    fun createMethodLocation(methodParameter: KParameter): MethodLocation {
        return MethodLocation(method, methodParameter)
    }

    fun newConceptNamesAndExpectedConceptNamesFromSuperiorBuilder(): Map<Alias, ConceptName> {
        val newConceptsFromMethod: Map<Alias, ConceptName> = newConcepts()
        val expectedConceptsFromSuperiorMethod: Map<Alias, ConceptName> =
            builderClassInterpreter.newConceptNamesFromSuperiorBuilderFilteredByExpectedAliases()
        return newConceptsFromMethod + expectedConceptsFromSuperiorMethod
    }

    private fun newConceptsAsPair(): List<Pair<Alias, ConceptName>> {
        return method.annotations
            .filterIsInstance<NewConcept>()
            .map { Pair(it.declareConceptAlias.toAlias(), it.concept.toConceptName()) }
    }

    fun newConcepts(): Map<Alias, ConceptName> {
        return newConceptsAsPair()
            .associate { it }
    }

    fun newConceptAliasesIncludingDuplicates(): List<Alias> {
        return newConceptsAsPair()
            .map { it.first }
    }

    fun newConceptAliases(): Set<Alias> {
        return newConcepts().keys
    }

    fun newConceptByAlias(alias: Alias): ConceptName {
        return requireNotNull(newConcepts()[alias]) {
            "No concept found for alias $alias in ${newConcepts()}."
        }
    }

    fun aliasesToSetRandomConceptIdentifierValueIncludingDuplicates(): List<Alias> {
        return method.annotations
            .filterIsInstance<SetRandomConceptIdentifierValue>()
            .map { it.conceptToModifyAlias.toAlias()}
    }

    fun aliasesToSetRandomConceptIdentifierValue(): Set<Alias> {
        return aliasesToSetRandomConceptIdentifierValueIncludingDuplicates().toSet()
    }

    fun aliasesToSetConceptIdentifierValueAliasesIncludingDuplicates(): List<Alias> {
        return method.valueParameters
            .flatMap { parameter -> parameter.annotations.filterIsInstance<SetConceptIdentifierValue>() }
            .map { it.conceptToModifyAlias.toAlias()}
    }

    private fun getFixedFacetValues(dataContext: DataContext? = null): List<FacetValueAnnotationContent> {
        val facetValues: MutableList<FacetValueAnnotationContent> = mutableListOf()
        val methodLocation = MethodLocation(method)

        method.annotations.filterIsInstance<SetFixedBooleanFacetValue>().forEach { annotation ->
            facetValues.add(
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
            )
        }

        method.annotations.filterIsInstance<SetFixedEnumFacetValue>().forEach { annotation ->
            val facetName = annotation.facetToModify.toFacetName()
            val enumValue = enumValueByString(annotation.value, facetName)

            facetValues.add(
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
            )
        }

        method.annotations.filterIsInstance<SetFixedIntFacetValue>().forEach { annotation ->
            facetValues.add(
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
            )
        }

        method.annotations.filterIsInstance<SetFixedStringFacetValue>().forEach { annotation ->
            facetValues.add(
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
            )
        }

        method.annotations.filterIsInstance<SetAliasConceptIdentifierReferenceFacetValue>().forEach { annotation ->
            val referencedAlias = annotation.referencedConceptAlias.toAlias()
            val referenceConceptId = dataContext?.conceptIdByAlias(referencedAlias)

            facetValues.add(
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
            )
        }
        return facetValues
    }

    private fun enumValueByString(enumValueString: String, facetName: FacetName):Enum<*>? {
        val facetSchema = schemaAccess.facetByFacetName(facetName)
        return facetSchema?.enumerationValues?.firstOrNull {
            it.name == enumValueString
        }
    }

    private fun getMethodParamAssignedFacetValues(dataContext: DataContext? = null): List<FacetValueAnnotationContent> {
        val facetValues: MutableList<FacetValueAnnotationContent> = mutableListOf()
        method.valueParameters.forEach { methodParameter ->
            val methodLocation = MethodLocation(method, methodParameter)
            methodParameter.annotations.filterIsInstance<SetFacetValue>().forEach { annotation ->
                val value = dataContext?.valueForMethodParameter(methodParameter)

                val ignoreNullValue = methodParameter.hasAnnotation<IgnoreNullFacetValue>()
                val facetValue = FacetValueAnnotationContent(
                    base = FacetValueAnnotationBaseData(
                        methodLocation = methodLocation,
                        alias = annotation.conceptToModifyAlias.toAlias(),
                        facetName = annotation.facetToModify.toFacetName(),
                        facetModificationRule = annotation.facetModificationRule,
                        annotation = annotation,
                        ignoreNullValue = ignoreNullValue,
                        type = methodParameter.type,
                        typeClass = null
                    ),
                    value = value
                )

                facetValues.add(facetValue)
            }
        }

        return facetValues
    }

    fun getFacetValueAnnotationContent(dataContext: DataContext? = null): List<FacetValueAnnotationContent> {
        return getFixedFacetValues(dataContext) + getMethodParamAssignedFacetValues(dataContext)
    }

    fun getManualAssignedConceptIdentifierAnnotationContent(dataContext: DataContext? = null): List<ConceptIdentifierAnnotationData> {
        val conceptIdentifierAssignments: MutableList<ConceptIdentifierAnnotationData> = mutableListOf()
        method.valueParameters.forEach { methodParameter ->
            val methodLocation = MethodLocation(method, methodParameter)
            methodParameter.annotations.filterIsInstance<SetConceptIdentifierValue>().forEach { annotation ->
                val ignoreNullValue = methodParameter.hasAnnotation<IgnoreNullFacetValue>()
                val conceptIdentifier = dataContext?.valueForMethodParameter(methodParameter)?.let { castConceptIdentifier(it) }
                val conceptIdentifierAssignmentData = ConceptIdentifierAnnotationData(
                    methodLocation = methodLocation,
                    alias = annotation.conceptToModifyAlias.toAlias(),
                    annotation = annotation,
                    ignoreNullValue = ignoreNullValue,
                    type = methodParameter.type,
                    conceptIdentifier = conceptIdentifier
                )

                conceptIdentifierAssignments.add(conceptIdentifierAssignmentData)
            }
        }

        return conceptIdentifierAssignments
    }

    private fun castConceptIdentifier(value: Any): ConceptIdentifier {
        return when(value) {
            is ConceptIdentifier -> value
            is String -> ConceptIdentifier.of(value)
            else -> throw IllegalArgumentException("Concept identifier must be a ${String::class} or a ${ConceptIdentifier::class}.")
        }
    }
}