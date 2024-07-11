package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet

/**
 * Represents a [EnumFacet] annotation.
 */
class EnumFacetAnnotationMirror(
    val enumerationClass: ClassMirror,
    minimumOccurrences: Int = 1,
    maximumOccurrences: Int = 1,
) : AbstractFacetAnnotationMirror(
    facetType = FacetType.TEXT_ENUMERATION,
    minimumOccurrences = minimumOccurrences,
    maximumOccurrences = maximumOccurrences,
)