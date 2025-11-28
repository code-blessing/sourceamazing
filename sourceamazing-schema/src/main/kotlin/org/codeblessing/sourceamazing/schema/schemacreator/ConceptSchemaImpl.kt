package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.ConceptSchema
import org.codeblessing.sourceamazing.schema.api.FacetName
import org.codeblessing.sourceamazing.schema.api.FacetSchema

class ConceptSchemaImpl(override val conceptName: ConceptName, override val facets: List<FacetSchema>) : ConceptSchema {
    override val facetNames: List<FacetName>
        get() = facets.map { it.facetName }.toList()

    override fun hasFacet(facetName: FacetName): Boolean {
        return facetNames.contains(facetName)
    }

    override fun facetByName(facetName: FacetName): FacetSchema {
        return facets.first { it.facetName == facetName }
    }
}
