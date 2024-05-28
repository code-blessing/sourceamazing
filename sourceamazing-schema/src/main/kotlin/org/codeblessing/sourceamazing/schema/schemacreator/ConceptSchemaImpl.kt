package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.ConceptSchema
import org.codeblessing.sourceamazing.schema.FacetSchema


class ConceptSchemaImpl(
    override val conceptName: ConceptName,
    override val facets: List<FacetSchema>,
    ): ConceptSchema
