package org.codeblessing.sourceamazing.schema.conceptgraph

import org.codeblessing.sourceamazing.schema.ConceptData
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetSchema
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.datacollection.validation.ConceptDataValidator
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.util.EnumUtil
import kotlin.reflect.KClass

object ConceptResolver {

    @Throws(DataValidationException::class)
    fun validateAndResolveConcepts(schema: SchemaAccess, conceptDataEntries: List<ConceptData>): ConceptGraph {
        val validatedDataEntries = ConceptDataValidator.validateEntries(schema, conceptDataEntries)
        val conceptNodeMap: Map<ConceptIdentifier, MutableConceptNode> = createConceptNodeMap(schema, validatedDataEntries)
        return ConceptGraph(conceptNodeMap)
    }

    private fun createConceptNodeMap(
        schema: SchemaAccess,
        conceptDataEntries: Map<ConceptIdentifier, ConceptData>
    ): Map<ConceptIdentifier, MutableConceptNode> {
        val conceptNodeMap: MutableMap<ConceptIdentifier, MutableConceptNode> = mutableMapOf()

        // 1. Phase: Create entry without facet values
        conceptDataEntries.forEach { (conceptIdentifier, conceptData) ->
            conceptNodeMap[conceptIdentifier] = MutableConceptNode(
                sequenceNumber = conceptData.sequenceNumber,
                conceptName = conceptData.conceptName,
                conceptIdentifier = conceptData.conceptIdentifier,
            )
        }

        // 2. Phase: Fill in facet values and connect with/resolve other referenced concept instances
        //          (resolve the concept identifier to the real concept node)
        conceptNodeMap.forEach {(conceptIdentifier, conceptNode) ->
            val conceptData = conceptDataEntries[conceptIdentifier]
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
        conceptNodeMap: MutableMap<ConceptIdentifier, MutableConceptNode>
    ): List<Any> {
        val facetName = facetSchema.facetName
        return when(facetSchema.facetType) {
            FacetType.TEXT,
            FacetType.NUMBER,
            FacetType.BOOLEAN, -> conceptData.getFacet(facetName)
            FacetType.TEXT_ENUMERATION -> transformEnumFacetValues(facetSchema, conceptData)
            FacetType.REFERENCE -> transformReferenceFacetValues(facetName, conceptData, conceptNodeMap)

        }
    }
    private fun transformEnumFacetValues(facetSchema: FacetSchema, conceptData: ConceptData): List<Any> {
        val enumerationType = facetSchema.enumerationType
            ?: throw IllegalStateException("Facet ${facetSchema.facetName} has no enumerationType.")
        return conceptData.getFacet(facetSchema.facetName)
            .map { value -> transformEnumFacetValue(enumerationType, value) }
    }

    private fun transformEnumFacetValue(enumerationType: KClass<*>, value: Any): Enum<*> {
        return requireNotNull(EnumUtil.fromAnyToEnum(value, enumerationType)) {
            "Could not convert enum value '$value' to enum constants ${EnumUtil.enumConstantList(enumerationType)} of $enumerationType"
        }
    }

    private fun transformReferenceFacetValues(
        facetName: FacetName,
        conceptData: ConceptData,
        conceptNodeMap: MutableMap<ConceptIdentifier, MutableConceptNode>
    ): List<Any> {
        return conceptData.getFacet(facetName)
            .filterIsInstance<ConceptIdentifier>()
            .map { referencingConceptIdentifier ->
                conceptNodeMap[referencingConceptIdentifier]
                    ?: throw IllegalStateException("Could not resolve reference to $referencingConceptIdentifier from $${conceptData.conceptIdentifier}. ")
            }.toList()
    }
}
