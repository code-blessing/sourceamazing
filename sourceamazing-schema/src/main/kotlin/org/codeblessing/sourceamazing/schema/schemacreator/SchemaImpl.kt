package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.api.schemaaccess.ConceptSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.FacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaAccess

data class SchemaImpl(private val rootConcept: ConceptName, private val concepts: Map<ConceptName, ConceptSchema>) :
    SchemaAccess {
    override fun conceptByConceptName(conceptName: ConceptName): ConceptSchema? {
        return concepts[conceptName]
    }

    override fun hasConceptName(conceptName: ConceptName): Boolean {
        return concepts.containsKey(conceptName)
    }

    override fun allConcepts(): Set<ConceptSchema> {
        return concepts.values.toSet()
    }

    fun numberOfConcepts(): Int {
        return concepts.size
    }

    override fun facetByFacetName(conceptName: ConceptName, facetName: FacetName): FacetSchema? {
        return conceptByConceptName(conceptName)?.facetByName(facetName)
    }

    override fun rootConceptName(): ConceptName {
        return rootConcept
    }
}
