package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.FacetName
import org.codeblessing.sourceamazing.schema.api.schemaaccess.FacetType
import org.codeblessing.sourceamazing.schema.api.schemaaccess.ReferenceFacetSchema

data class ReferenceFacetSchemaImpl(
    override val conceptName: ConceptName,
    override val facetName: FacetName,
    override val facetType: FacetType,
    override val minimumOccurrences: Int,
    override val maximumOccurrences: Int,
    override val referencingConcepts: Set<ConceptName>,
) : ReferenceFacetSchema
