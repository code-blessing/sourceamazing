package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.FacetSchema
import org.codeblessing.sourceamazing.api.process.schema.FacetTypeEnum
import kotlin.reflect.KClass

data class FacetSchemaImpl(
    override val facetName: FacetName,
    override val facetType: FacetTypeEnum,
    override val mandatory: Boolean,
    override val referencingConcept: ConceptName?,
    override val enumerationType: KClass<*>?
) : FacetSchema {
    override fun enumerationValues(): List<Enum<*>> {
        val enumerationType = enumerationType ?: return emptyList()
        return enumerationType.java.enumConstants.filterIsInstance(Enum::class.java)

    }
}
