package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet

/**
 * Represents a [ReferenceFacet] annotation.
 */
class ReferenceFacetAnnotationMirror(
    val referencedConcepts: List<ClassMirror>,
    minimumOccurrences: Int = 1,
    maximumOccurrences: Int = 1,
) : AbstractFacetAnnotationMirror(
    facetType = FacetType.REFERENCE,
    minimumOccurrences = minimumOccurrences,
    maximumOccurrences = maximumOccurrences,
)