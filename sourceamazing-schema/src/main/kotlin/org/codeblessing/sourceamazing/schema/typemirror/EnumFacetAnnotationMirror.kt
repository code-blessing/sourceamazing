package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.typemirror.provider.ClassMirrorProvider

/**
 * Represents a [EnumFacet] annotation.
 */
class EnumFacetAnnotationMirror(
    val enumerationClass: ClassMirrorProvider,
    minimumOccurrences: Int = 1,
    maximumOccurrences: Int = 1,
) : AbstractFacetAnnotationMirror(
    facetType = FacetType.TEXT_ENUMERATION,
    minimumOccurrences = minimumOccurrences,
    maximumOccurrences = maximumOccurrences,
)