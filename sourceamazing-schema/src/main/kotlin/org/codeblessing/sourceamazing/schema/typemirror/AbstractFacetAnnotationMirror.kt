package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.FacetType

abstract class AbstractFacetAnnotationMirror(
    val minimumOccurrences: Int,
    val maximumOccurrences: Int,
    val facetType: FacetType
) : AnnotationMirror