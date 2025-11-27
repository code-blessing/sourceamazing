package org.codeblessing.sourceamazing.schema.conceptgraph

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.datacollection.validation.ConceptDataValidator
import org.codeblessing.sourceamazing.utils.enumeration.EnumUtil

object ConceptResolver {

    @Throws(DataValidationException::class)
    fun validateAndResolveConcepts(
        schema: SchemaAccess,
        conceptDataEntries: List<ConceptData>,
    ): ConceptGraph {
        val validatedDataEntries = ConceptDataValidator.validateEntries(schema, conceptDataEntries)
        val conceptNodeMap: Map<ConceptIdentifier, MutableConceptNode> =
            createConceptNodeMap(schema, validatedDataEntries)
        return ConceptGraph(conceptNodeMap)
    }

    private fun createConceptNodeMap(
        schema: SchemaAccess,
        conceptDataEntries: Map<ConceptIdentifier, ConceptData>,
    ): Map<ConceptIdentifier, MutableConceptNode> {
        val conceptNodeMap: MutableMap<ConceptIdentifier, MutableConceptNode> = mutableMapOf()

        // 1. Phase: Create entry without facet values
        conceptDataEntries.forEach { (conceptIdentifier, conceptData) ->
            conceptNodeMap[conceptIdentifier] =
                MutableConceptNode(
                    sequenceNumber = conceptData.sequenceNumber,
                    conceptName = conceptData.conceptName,
                    conceptIdentifier = conceptData.conceptIdentifier,
                )
        }

        // 2. Phase: Fill in facet values and connect with/resolve other referenced concept
        // instances
        //          (resolve the concept identifier to the real concept node)
        conceptNodeMap.forEach { (conceptIdentifier, conceptNode) ->
            val conceptData =
                conceptDataEntries[conceptIdentifier]
                    ?: throw IllegalStateException("Could not resolve $conceptIdentifier. ")
            val conceptSchema = schema.conceptByConceptName(conceptNode.conceptName)
            conceptSchema.facets.forEach { facetSchema ->
                conceptNode.facetValues[facetSchema.facetName] =
                    transformFacetValues(facetSchema, conceptData, conceptNodeMap)
            }
        }

        return conceptNodeMap
    }

    private fun transformFacetValues(
        facetSchema: FacetSchema,
        conceptData: ConceptData,
        conceptNodeMap: MutableMap<ConceptIdentifier, MutableConceptNode>,
    ): List<Any> {
        val facetName = facetSchema.facetName
        return when (facetSchema) {
            is TextFacetSchema,
            is NumberFacetSchema,
            is BooleanFacetSchema -> conceptData.getFacet(facetName)
            is EnumFacetSchema -> transformEnumFacetValues(facetSchema, conceptData)
            is ReferenceFacetSchema ->
                transformReferenceFacetValues(facetName, conceptData, conceptNodeMap)
        }
    }

    private fun transformEnumFacetValues(
        facetSchema: EnumFacetSchema,
        conceptData: ConceptData,
    ): List<Any> {
        val enumerationType = facetSchema.enumerationType
        return conceptData.getFacet(facetSchema.facetName).map { value ->
            transformEnumFacetValue(enumerationType, value)
        }
    }

    private fun transformEnumFacetValue(enumerationType: KClass<*>, value: Any): Enum<*> {
        return requireNotNull(EnumUtil.fromAnyToEnum(value, enumerationType)) {
            "Could not convert enum value '$value' to enum constants ${EnumUtil.enumConstantList(enumerationType)} of $enumerationType"
        }
    }

    private fun transformReferenceFacetValues(
        facetName: FacetName,
        conceptData: ConceptData,
        conceptNodeMap: MutableMap<ConceptIdentifier, MutableConceptNode>,
    ): List<Any> {
        return conceptData
            .getFacet(facetName)
            .filterIsInstance<ConceptIdentifier>()
            .map { referencingConceptIdentifier ->
                conceptNodeMap[referencingConceptIdentifier]
                    ?: throw IllegalStateException(
                        "Could not resolve reference to $referencingConceptIdentifier from $${conceptData.conceptIdentifier}. "
                    )
            }
            .toList()
    }
}
