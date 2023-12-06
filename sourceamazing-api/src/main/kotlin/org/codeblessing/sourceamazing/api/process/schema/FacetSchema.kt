package org.codeblessing.sourceamazing.api.process.schema

import org.codeblessing.sourceamazing.api.process.schema.annotations.FacetType
import kotlin.reflect.KClass


interface FacetSchema {
    val facetName: FacetName
    val facetType: FacetType
    val minimumOccurrences: Int
    val maximumOccurrences: Int
    val referencingConcepts: Set<ConceptName>
    val enumerationType: KClass<*>?
    fun enumerationValues(): List<Enum<*>>
}
