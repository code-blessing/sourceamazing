package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.FacetName
import org.codeblessing.sourceamazing.schema.api.schemaaccess.BooleanFacetSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.FacetType

data class BooleanFacetSchemaImpl(
    override val conceptName: ConceptName,
    override val facetName: FacetName,
    override val facetType: FacetType,
    override val minimumOccurrences: Int,
    override val maximumOccurrences: Int,
) : BooleanFacetSchema
