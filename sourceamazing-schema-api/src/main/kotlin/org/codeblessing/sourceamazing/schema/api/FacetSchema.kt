package org.codeblessing.sourceamazing.schema.api

import kotlin.reflect.KClass

sealed interface FacetSchema {
    val conceptName: ConceptName
    val facetName: FacetName
    val facetType: FacetType
    val minimumOccurrences: Int
    val maximumOccurrences: Int
}

interface TextFacetSchema : FacetSchema

interface NumberFacetSchema : FacetSchema

interface BooleanFacetSchema : FacetSchema

interface EnumFacetSchema : FacetSchema {
    val enumerationType: KClass<*>?
    val enumerationValues: List<Enum<*>>
}

interface ReferenceFacetSchema : FacetSchema {
    val referencingConcepts: Set<ConceptName>
}
