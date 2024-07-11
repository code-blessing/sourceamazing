package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet

/**
 * Represents a [BooleanFacet] annotation.
 */
class BooleanFacetAnnotationMirror(
    minimumOccurrences: Int = 1,
    maximumOccurrences: Int = 1,
) : AbstractFacetAnnotationMirror(
    facetType = FacetType.BOOLEAN,
    minimumOccurrences = minimumOccurrences,
    maximumOccurrences = maximumOccurrences,
)