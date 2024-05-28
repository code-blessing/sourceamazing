package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.ConceptData
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName

class ConceptDataImpl(
    override val sequenceNumber: Int,
    override val conceptName: ConceptName,
    override val conceptIdentifier: ConceptIdentifier,
): ConceptData {
    private val mutableFacets: MutableMap<FacetName, MutableList<Any>> = mutableMapOf()

    override fun hasFacet(facetName: FacetName): Boolean {
        return mutableFacets.containsKey(facetName)
    }

    override fun getFacet(facetName: FacetName): List<Any> {
        return mutableFacets[facetName] ?: emptyList()
    }

    override fun getFacetNames(): Set<FacetName> {
        return mutableFacets.keys
    }

    override fun allFacets(): Map<FacetName, List<Any>> {
        return mutableFacets.toMap()
    }


    override fun replaceFacetValues(facetName: FacetName, facetValues: List<Any>): ConceptDataImpl {
        mutableFacets[facetName] = facetValues.toMutableList()
        return this
    }


    override fun addFacetValue(facetName: FacetName, facetValue: Any): ConceptDataImpl {
        assureFacetList(facetName).add(facetValue)
        return this
    }

    override fun addFacetValues(facetName: FacetName, facetValues: List<Any>): ConceptData {
        assureFacetList(facetName).addAll(facetValues)
        return this
    }

    override fun describe(): String {
        val facetDescription = mutableFacets
            .map { (key, value) -> describeFacet(key, value) }
            .joinToString("\n")

        return "${conceptName.simpleName()}:${conceptIdentifier.name} {\n$facetDescription\n}"
    }

    private fun describeFacet(key: FacetName, value: MutableList<Any>): String {
        return "  ${key.simpleName()}:[ ${value.joinToString(", ") { "'${it}'" }} ]"
    }

    private fun assureFacetList(facetName: FacetName): MutableList<Any> {
        val currentList = mutableFacets[facetName]
        return currentList ?: createEmptyListForFacet(facetName)
    }

    private fun createEmptyListForFacet(facetName: FacetName): MutableList<Any> {
        val list = mutableListOf<Any>()
        mutableFacets[facetName] = list
        return list
    }
}
