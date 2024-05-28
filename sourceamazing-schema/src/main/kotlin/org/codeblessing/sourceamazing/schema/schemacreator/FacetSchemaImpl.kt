package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetSchema
import org.codeblessing.sourceamazing.schema.FacetType
import kotlin.reflect.KClass

data class FacetSchemaImpl(
    override val facetName: FacetName,
    override val facetType: FacetType,
    override val minimumOccurrences: Int,
    override val maximumOccurrences: Int,
    override val referencingConcepts: Set<ConceptName>,
    override val enumerationType: KClass<*>
) : FacetSchema {
    override fun enumerationValues(): List<Enum<*>> {
        if(enumerationType == Unit::class) {
            return emptyList()
        }
        return enumerationType.java.enumConstants.filterIsInstance(Enum::class.java)

    }
}
