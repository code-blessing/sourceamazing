package org.codeblessing.sourceamazing.engine.process.conceptgraph

import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.FacetSchema
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.api.process.schema.annotations.FacetType
import org.codeblessing.sourceamazing.engine.process.datacollection.validation.ConceptDataValidator
import org.codeblessing.sourceamazing.engine.process.datacollection.validation.exceptions.SchemaValidationException
import org.codeblessing.sourceamazing.engine.process.util.EnumUtil
import kotlin.reflect.KClass

object ConceptResolver {

    @Throws(SchemaValidationException::class)
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
            conceptNodeMap[conceptIdentifier] = MutableConceptNode(conceptData.sequenceNumber, conceptData.conceptName, conceptData.conceptIdentifier)
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
            .map { value -> if(value is String) fromStringToEnum(value, enumerationType) else value }
    }

    private fun fromStringToEnum(enumStringValue: String, enumerationType: KClass<*>): Any {
        return EnumUtil.fromStringToEnum(enumStringValue, enumerationType)
            ?: throw IllegalStateException("Could not convert enum value '$enumStringValue' to enum constants of $enumerationType")
    }

    private fun transformReferenceFacetValues(
        facetName: FacetName,
        conceptData: ConceptData,
        conceptNodeMap: MutableMap<ConceptIdentifier, MutableConceptNode>
    ): List<Any> {
        return conceptData.getFacet(facetName)
            .filterIsInstance(ConceptIdentifier::class.java)
            .map { referencingConceptIdentifier ->
                conceptNodeMap[referencingConceptIdentifier]
                    ?: throw IllegalStateException("Could not resolve reference to $referencingConceptIdentifier from $${conceptData.conceptIdentifier}. ")
            }.toList()
    }
}
