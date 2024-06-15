package org.codeblessing.sourceamazing.schema

import kotlin.reflect.KClass


interface FacetSchema {
    val facetName: FacetName
    val facetType: FacetType
    val minimumOccurrences: Int
    val maximumOccurrences: Int
    val referencingConcepts: Set<ConceptName>
    val enumerationType: KClass<*>?
    val enumerationValues: List<Enum<*>>
}
