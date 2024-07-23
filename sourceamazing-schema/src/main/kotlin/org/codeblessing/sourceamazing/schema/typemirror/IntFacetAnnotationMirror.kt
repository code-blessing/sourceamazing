package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet

/**
 * Represents a [IntFacet] annotation.
 */
class IntFacetAnnotationMirror(
    minimumOccurrences: Int = 1,
    maximumOccurrences: Int = 1,
) : AbstractFacetAnnotationMirror(
    IntFacet::class,
    minimumOccurrences = minimumOccurrences,
    maximumOccurrences = maximumOccurrences,
    facetType = FacetType.NUMBER,
)