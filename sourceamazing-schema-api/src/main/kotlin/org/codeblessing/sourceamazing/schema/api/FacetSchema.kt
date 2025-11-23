package org.codeblessing.sourceamazing.schema.api

import kotlin.reflect.KClass

// TODO Create a sealed interface for the various types (at least enum, reference, rest)
interface FacetSchema {
    // TODO add conceptName to make the facet unique
    val facetName: FacetName
    val facetType: FacetType
    val minimumOccurrences: Int
    val maximumOccurrences: Int
    val referencingConcepts: Set<ConceptName>
    val enumerationType: KClass<*>?
    val enumerationValues: List<Enum<*>>
}
