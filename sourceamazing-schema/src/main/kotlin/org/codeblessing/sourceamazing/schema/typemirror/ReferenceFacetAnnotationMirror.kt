package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

/**
 * Represents a [ReferenceFacet] annotation.
 */
class ReferenceFacetAnnotationMirror(
    val referencedConcepts: List<MirrorProvider<ClassMirrorInterface>>,
    minimumOccurrences: Int = 1,
    maximumOccurrences: Int = 1,
) : AbstractFacetAnnotationMirror(
    ReferenceFacet::class,
    minimumOccurrences = minimumOccurrences,
    maximumOccurrences = maximumOccurrences,
    facetType = FacetType.REFERENCE,
)