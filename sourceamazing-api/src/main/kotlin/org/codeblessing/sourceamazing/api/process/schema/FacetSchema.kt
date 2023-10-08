package org.codeblessing.sourceamazing.api.process.schema

import kotlin.reflect.KClass


interface FacetSchema {
    val facetName: FacetName
    val facetType: FacetTypeEnum
    val mandatory: Boolean
    val referencingConcept: ConceptName?
    val enumerationType: KClass<*>?
    fun enumerationValues(): List<Enum<*>>
}
