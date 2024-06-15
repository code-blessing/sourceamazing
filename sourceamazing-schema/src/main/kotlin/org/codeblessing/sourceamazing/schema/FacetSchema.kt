package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface


interface FacetSchema {
    val facetName: FacetName
    val facetType: FacetType
    val minimumOccurrences: Int
    val maximumOccurrences: Int
    val referencingConcepts: Set<ConceptName>
    val enumerationType: ClassMirrorInterface?
    val enumerationValues: List<String>
}
