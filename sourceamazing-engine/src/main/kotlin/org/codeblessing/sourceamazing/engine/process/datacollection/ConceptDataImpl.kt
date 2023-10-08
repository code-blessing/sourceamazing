package org.codeblessing.sourceamazing.engine.process.datacollection

import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName

class ConceptDataImpl(
    override val sequenceNumber: Int,
    override val conceptName: ConceptName,
    override val conceptIdentifier: ConceptIdentifier,
): ConceptData {
    private var mutableParentConceptIdentifier: ConceptIdentifier? = null
    private val mutableFacets: MutableMap<FacetName, Any?> = mutableMapOf()
    override val parentConceptIdentifier: ConceptIdentifier?
        get() = mutableParentConceptIdentifier

    override fun setParentConceptIdentifier(parentConceptIdentifier: ConceptIdentifier?): ConceptData {
        this.mutableParentConceptIdentifier = parentConceptIdentifier
        return this
    }


    override fun hasFacet(facetName: FacetName): Boolean {
        return mutableFacets.containsKey(facetName)
    }

    override fun getFacet(facetName: FacetName): Any? {
        return mutableFacets[facetName]
    }

    override fun getFacetNames(): Set<FacetName> {
        return mutableFacets.keys
    }

    override fun allFacets(): Map<FacetName, Any?> {
        return mutableFacets.toMap()
    }


    override fun addOrReplaceFacetValues(facetValues: Map<FacetName, Any?>): ConceptDataImpl {
        facetValues.forEach { (facetName, facetValue) ->
            mutableFacets[facetName] = facetValue
        }
        return this
    }


    override fun addOrReplaceFacetValue(facetName: FacetName, facetValue: Any?): ConceptDataImpl {
        mutableFacets[facetName] = facetValue
        return this
    }
}
