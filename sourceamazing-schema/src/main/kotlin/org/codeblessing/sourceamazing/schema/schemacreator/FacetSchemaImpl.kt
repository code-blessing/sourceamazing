package org.codeblessing.sourceamazing.schema.schemacreator

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.FacetName
import org.codeblessing.sourceamazing.schema.api.FacetSchema
import org.codeblessing.sourceamazing.schema.api.FacetType
import org.codeblessing.sourceamazing.utils.type.enumValues

data class FacetSchemaImpl(
    override val conceptName: ConceptName,
    override val facetName: FacetName,
    override val facetType: FacetType,
    override val minimumOccurrences: Int,
    override val maximumOccurrences: Int,
    override val referencingConcepts: Set<ConceptName>,
    override val enumerationType: KClass<*>?,
) : FacetSchema {
    override val enumerationValues: List<Enum<*>> = enumerationType?.enumValues ?: emptyList()
}
