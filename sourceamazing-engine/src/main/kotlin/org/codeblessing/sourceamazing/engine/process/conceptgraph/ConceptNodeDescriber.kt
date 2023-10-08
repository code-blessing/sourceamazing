package org.codeblessing.sourceamazing.engine.process.conceptgraph

object ConceptNodeDescriber {

    fun createConceptNodeDescription(conceptNode: ConceptNode): String {
        val selfDescription = createShortDescription(conceptNode)
        val parentConceptNode = conceptNode.parentConceptNode
        val parentDescription = if(parentConceptNode == null) "Root" else createShortDescription(parentConceptNode)
        val facetValues = conceptNode.facetValues.map { (key, value) -> "${key.name}:$value" }.joinToString(",")

        return "$selfDescription (Parent:$parentDescription) [$facetValues]"

    }

    private fun createShortDescription(conceptNode: ConceptNode): String {
        return "${conceptNode.conceptIdentifier}(${conceptNode.conceptName})"
    }
}
