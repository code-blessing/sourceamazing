package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.ConceptSchema
import org.codeblessing.sourceamazing.api.process.schema.FacetSchema


class ConceptSchemaImpl(
    override val conceptName: ConceptName,
    override val facets: List<FacetSchema>,
    ): ConceptSchema
