package org.codeblessing.sourceamazing.engine.process.conceptgraph

import org.codeblessing.sourceamazing.api.process.conceptgraph.exceptions.*
import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.datacollection.exceptions.SchemaValidationException
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptSchema
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataValidator

object ConceptResolver {

    @Throws(SchemaValidationException::class, ConceptGraphException::class)
    fun validateAndResolveConcepts(schema: SchemaAccess, conceptDataEntries: List<ConceptData>): ConceptGraph {
        conceptDataEntries.forEach { ConceptDataValidator.validateSingleEntry(schema, it) }
        val conceptDataMap = conceptDataEntries.associateBy { it.conceptIdentifier }
        val conceptNodeMap: Map<ConceptIdentifier, MutableConceptNode> = createConceptNodeMap(conceptDataEntries)
        val childrenConceptsMap = createChildrenConceptNodeMap(conceptDataEntries, conceptNodeMap)

        conceptNodeMap.values.forEach { conceptNode ->
            val conceptSchema = schema.conceptByConceptName(conceptNode.conceptName)
            val conceptData = requireNotNull(conceptDataMap[conceptNode.conceptIdentifier])

            // resolve parent concept node (if not root node)
            val parentConceptIdentifier = conceptData.parentConceptIdentifier
            if(parentConceptIdentifier != null) {
                conceptNode.parentConceptNode = conceptNodeMap[parentConceptIdentifier]
                    ?: throw ParentConceptNotFoundConceptGraphException(
                        conceptName = conceptNode.conceptName,
                        conceptIdentifier = conceptNode.conceptIdentifier,
                        parentConceptIdentifier = parentConceptIdentifier)
            }
            // resolve children concept nodes
            conceptNode.childrenConceptNodes = childrenConceptsMap[conceptNode.conceptIdentifier]
                ?.groupBy { it.conceptName }
                ?: emptyMap()

            // resolve all facet values
            conceptSchema.facetNames.forEach { facetName ->
                val facetValue = conceptData.getFacet(facetName)
                if(facetValue is ConceptIdentifier) {
                    val referencedConceptNode = conceptNodeMap[facetValue]
                        ?: throw ReferencedConceptConceptGraphNodeNotFoundException(
                            conceptName = conceptNode.conceptName,
                            conceptIdentifier = conceptNode.conceptIdentifier,
                            facetName = facetName,
                            referencedConceptIdentifier = facetValue,
                        )
                    conceptNode.facetValues[facetName] = referencedConceptNode
                } else {
                    conceptNode.facetValues[facetName] = facetValue
                }
            }
        }

        val conceptGraph = ConceptGraph(conceptNodeMap)
        validateConceptGraph(schema, conceptGraph)
        return conceptGraph
    }

    @Throws(ConceptGraphException::class)
    private fun validateConceptGraph(schema: SchemaAccess, conceptGraph: ConceptGraph) {
        val rootConceptsByConceptName = conceptGraph.rootConcepts().groupBy { it.conceptName }

        schema.allRootConcepts().forEach { rootConceptSchema ->
            val conceptNodesForSchema: List<ConceptNode> = rootConceptsByConceptName[rootConceptSchema.conceptName] ?: emptyList()
            validateMinMaxOccurrence(rootConceptSchema, conceptNodesForSchema)
            conceptNodesForSchema.forEach { rootConceptNode -> validateChildNodes(schema, conceptGraph, rootConceptNode) }
        }
    }

    private fun validateChildNodes(schema: SchemaAccess, conceptGraph: ConceptGraph, conceptNode: ConceptNode){
        val conceptSchema = schema.conceptByConceptName(conceptNode.conceptName)
        schema.allChildrenConcepts(conceptSchema).forEach { childConceptSchema ->
            val childrenConceptNodes = conceptGraph.childConcepts(childConceptSchema.conceptName, conceptNode.conceptIdentifier)
            validateMinMaxOccurrence(childConceptSchema, childrenConceptNodes)
            childrenConceptNodes.forEach { childConceptNode -> validateChildNodes(schema, conceptGraph, childConceptNode) }
        }
    }

    @Throws(ConceptGraphException::class)
    private fun validateMinMaxOccurrence(conceptSchema: ConceptSchema, conceptNodes: List<ConceptNode>) {
        val range = conceptSchema.minOccurrence..conceptSchema.maxOccurrence
        if(!range.contains(conceptNodes.size)) {
            throw OccurrenceRangeConceptGraphException(conceptSchema.conceptName,
                minOccurrence = conceptSchema.minOccurrence,
                maxOccurrence = conceptSchema.maxOccurrence,
                conceptNodes.map { ConceptNodeDescriber.createConceptNodeDescription(it) })
        }
    }



    private fun createConceptNodeMap(conceptDataEntries: List<ConceptData>): Map<ConceptIdentifier, MutableConceptNode> {
        val conceptNodeMap: MutableMap<ConceptIdentifier, MutableConceptNode> = mutableMapOf()
        conceptDataEntries.forEach { conceptData ->
            val conceptIdentifier = conceptData.conceptIdentifier
            if(conceptNodeMap.containsKey(conceptIdentifier)) {
                throw DuplicateConceptIdentifierFoundConceptGraphException(conceptData.conceptName, conceptIdentifier)
            }
            conceptNodeMap[conceptIdentifier] = MutableConceptNode(conceptData.sequenceNumber, conceptData.conceptName, conceptData.conceptIdentifier)
        }
        return conceptNodeMap
    }

    private fun createChildrenConceptNodeMap(
        conceptDataEntries: List<ConceptData>,
        conceptNodeMap: Map<ConceptIdentifier, MutableConceptNode>
    ): Map<ConceptIdentifier, List<MutableConceptNode>> {
        return conceptDataEntries
            .filter { it.parentConceptIdentifier != null  }
            .groupBy(
                keySelector = { requireNotNull(it.parentConceptIdentifier) },
                valueTransform = { requireNotNull(conceptNodeMap[it.conceptIdentifier])
                }
            )
    }
}
