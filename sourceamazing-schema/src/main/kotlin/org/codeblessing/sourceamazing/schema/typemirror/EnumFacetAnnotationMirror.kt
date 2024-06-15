package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

/**
 * Represents a [EnumFacet] annotation.
 */
class EnumFacetAnnotationMirror(
    val enumerationClass: MirrorProvider<ClassMirrorInterface>,
    minimumOccurrences: Int = 1,
    maximumOccurrences: Int = 1,
) : AbstractFacetAnnotationMirror(
    EnumFacet::class,
    minimumOccurrences = minimumOccurrences,
    maximumOccurrences = maximumOccurrences,
    facetType = FacetType.TEXT_ENUMERATION,
)