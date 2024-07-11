package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue

/**
 * Represents a [QueryFacetValue] annotation.
 */
class QueryFacetValueAnnotationMirror(val facetClass: ClassMirror) : AnnotationMirror