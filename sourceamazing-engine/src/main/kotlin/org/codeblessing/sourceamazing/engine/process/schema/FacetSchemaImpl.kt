package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.FacetSchema
import org.codeblessing.sourceamazing.api.process.schema.annotations.FacetType
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
