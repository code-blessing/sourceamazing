package org.codeblessing.sourceamazing.schema.api.schemaaccess

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.FacetName

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
    val enumerationClass: KClass<*>
    val enumerationValues: List<Enum<*>>
}

interface ReferenceFacetSchema : FacetSchema {
    val referencingConcepts: Set<ConceptName>
}
