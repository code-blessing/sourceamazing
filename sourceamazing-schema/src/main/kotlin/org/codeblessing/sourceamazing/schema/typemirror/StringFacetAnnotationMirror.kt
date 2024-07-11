package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet

/**
 * Represents a [StringFacet] annotation.
 */
class StringFacetAnnotationMirror(minimumOccurrences: Int = 1,
                                  maximumOccurrences: Int = 1,
    ) : AbstractFacetAnnotationMirror(
    facetType = FacetType.TEXT,
    minimumOccurrences = minimumOccurrences,
    maximumOccurrences = maximumOccurrences,
)