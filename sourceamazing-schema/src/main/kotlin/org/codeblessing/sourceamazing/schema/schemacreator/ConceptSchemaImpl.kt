package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.ConceptSchema
import org.codeblessing.sourceamazing.schema.api.FacetSchema

class ConceptSchemaImpl(
    override val conceptName: ConceptName,
    override val facets: List<FacetSchema>,
) : ConceptSchema
