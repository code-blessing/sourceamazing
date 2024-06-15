package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetSchema
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.type.enumValues
import kotlin.reflect.KClass

data class FacetSchemaImpl(
    override val facetName: FacetName,
    override val facetType: FacetType,
    override val minimumOccurrences: Int,
    override val maximumOccurrences: Int,
    override val referencingConcepts: Set<ConceptName>,
    override val enumerationType: KClass<*>?
) : FacetSchema {
    override val enumerationValues: List<Enum<*>> = enumerationType?.enumValues ?: emptyList()
}
