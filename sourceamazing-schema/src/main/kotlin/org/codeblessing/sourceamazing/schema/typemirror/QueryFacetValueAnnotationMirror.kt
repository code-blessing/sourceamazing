package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

/**
 * Represents a [QueryFacetValue] annotation.
 */
class QueryFacetValueAnnotationMirror(val facetClass: MirrorProvider<ClassMirrorInterface>)
    : AnnotationMirror(annotationClass = QueryFacetValue::class)