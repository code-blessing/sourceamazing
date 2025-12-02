package org.codeblessing.sourceamazing.schema.schemacreator

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.EnumFacetSchema
import org.codeblessing.sourceamazing.schema.api.FacetName
import org.codeblessing.sourceamazing.schema.api.FacetType
import org.codeblessing.sourceamazing.utils.type.enumValues

data class EnumFacetSchemaImpl(
    override val conceptName: ConceptName,
    override val facetName: FacetName,
    override val facetType: FacetType,
    override val minimumOccurrences: Int,
    override val maximumOccurrences: Int,
    override val enumerationClass: KClass<*>,
) : EnumFacetSchema {
    override val enumerationValues: List<Enum<*>> = enumerationClass.enumValues
}
