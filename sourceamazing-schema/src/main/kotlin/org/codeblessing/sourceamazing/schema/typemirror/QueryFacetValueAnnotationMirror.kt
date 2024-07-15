package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.typemirror.provider.ClassMirrorProvider

/**
 * Represents a [QueryFacetValue] annotation.
 */
class QueryFacetValueAnnotationMirror(val facetClass: ClassMirrorProvider) : AnnotationMirror