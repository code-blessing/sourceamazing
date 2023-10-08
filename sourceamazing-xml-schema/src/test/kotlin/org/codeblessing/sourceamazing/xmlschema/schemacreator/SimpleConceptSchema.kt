package org.codeblessing.sourceamazing.xmlschema.schemacreator

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.ConceptSchema
import org.codeblessing.sourceamazing.api.process.schema.FacetSchema

class SimpleConceptSchema(
    override val conceptName: ConceptName,
    override val conceptClass: Class<*>,
    override val parentConceptName: ConceptName?,
    override val facets: List<FacetSchema>,
    override val minOccurrence: Int = 0,
    override val maxOccurrence: Int = Int.MAX_VALUE
) : ConceptSchema
