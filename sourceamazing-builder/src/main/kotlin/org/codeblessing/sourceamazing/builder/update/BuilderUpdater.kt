package org.codeblessing.sourceamazing.builder.update

import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.validation.BuilderCollectionHelper
import org.codeblessing.sourceamazing.builder.validation.BuilderMethodInterpreter
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.util.ConceptIdentifierUtil
import kotlin.reflect.KFunction

object BuilderUpdater {

    fun updateConceptDataCollector(builderMethodInterpreter: BuilderMethodInterpreter, builderInterpreterDataCollector: BuilderMethodInterpreterDataCollector) {
        addNewConceptDataWithDedicatedConceptIdentifier(builderMethodInterpreter, builderInterpreterDataCollector)
        addNewConceptDataWithRandomConceptIdentifier(builderMethodInterpreter, builderInterpreterDataCollector)
        updateFacetValues(builderMethodInterpreter, builderInterpreterDataCollector)
    }

    private fun addNewConceptDataWithDedicatedConceptIdentifier(
        builderMethodInterpreter: BuilderMethodInterpreter,
        builderInterpreterDataCollector: BuilderMethodInterpreterDataCollector,
    ) {
        val method: KFunction<*> = builderMethodInterpreter.method
        val dataContext = builderInterpreterDataCollector.getDataContext()

        builderMethodInterpreter.getManualAssignedConceptIdentifierAnnotationContent(dataContext).forEach { conceptIdentifierAnnotationData ->
            val conceptAlias = conceptIdentifierAnnotationData.alias
            val conceptName = getConceptByAlias(conceptAlias, builderMethodInterpreter)
            val conceptIdentifier = conceptIdentifierAnnotationData.conceptIdentifier
                ?: throw IllegalArgumentException("Can not pass null value as concept identifier argument on method $method")
            builderInterpreterDataCollector.newConceptData(conceptAlias, conceptName, conceptIdentifier)

        }
    }

    private fun addNewConceptDataWithRandomConceptIdentifier(
        builderMethodInterpreter: BuilderMethodInterpreter,
        builderInterpreterDataCollector: BuilderMethodInterpreterDataCollector,
    ) {
        val aliasesToSetRandomConceptIdentifierValue = builderMethodInterpreter.aliasesToSetRandomConceptIdentifierValue()
        aliasesToSetRandomConceptIdentifierValue.forEach { conceptAlias ->
            val conceptName = getConceptByAlias(conceptAlias, builderMethodInterpreter)
            val conceptIdentifier = ConceptIdentifierUtil.random(conceptName)
            builderInterpreterDataCollector.newConceptData(conceptAlias, conceptName, conceptIdentifier)
        }
    }

    private fun updateFacetValues(builderMethodInterpreter: BuilderMethodInterpreter, builderInterpreterDataCollector: BuilderMethodInterpreterDataCollector) {
        val dataContext = builderInterpreterDataCollector.getDataContext()
        builderMethodInterpreter.getFacetValueAnnotationContent(dataContext).forEach { facetValueAnnotationContent ->
            val value: Any = facetValueAnnotationContent.value
                ?: if(!facetValueAnnotationContent.base.ignoreNullValue) {
                    throw IllegalArgumentException("Can not pass null values at '${facetValueAnnotationContent.base.methodLocation}' " +
                            "on method ${builderMethodInterpreter.method}. If this is wanted, use the annotation '${IgnoreNullFacetValue::class.annotationText()}'.")

                } else {
                    return@forEach // skip null values silently
                }

            updateConceptData(
                conceptAlias = facetValueAnnotationContent.base.alias,
                facetName = facetValueAnnotationContent.base.facetName,
                value = value,
                facetModificationRule = facetValueAnnotationContent.base.facetModificationRule,
                builderInterpreterDataCollector = builderInterpreterDataCollector,
            )
        }
    }

    private fun updateConceptData(
        conceptAlias: Alias,
        facetName: FacetName,
        value: Any,
        facetModificationRule: FacetModificationRule,
        builderInterpreterDataCollector: BuilderMethodInterpreterDataCollector,
    ) {
        val conceptId: ConceptIdentifier = builderInterpreterDataCollector.conceptIdByAlias(conceptAlias)
        val conceptData = builderInterpreterDataCollector.existingConceptData(conceptId)
        val facetValues = BuilderCollectionHelper.facetValueListFromFacetValue(value)
        when(facetModificationRule) {
            FacetModificationRule.ADD -> conceptData.addFacetValues(facetName, facetValues)
            FacetModificationRule.REPLACE -> conceptData.replaceFacetValues(facetName, facetValues)
        }
        builderInterpreterDataCollector.validateAfterUpdate(conceptData)
    }

    private fun getConceptByAlias(conceptAlias: Alias, builderMethodInterpreter: BuilderMethodInterpreter): ConceptName {
        val newConceptsByAlias: Map<Alias, ConceptName> = builderMethodInterpreter.newConcepts()

        return newConceptsByAlias[conceptAlias]
            ?: throw IllegalStateException("Can not find concept name for alias '$conceptAlias' on method ${builderMethodInterpreter.method}")
    }
}