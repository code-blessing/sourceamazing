package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror

/**
 * Represents a [IgnoreNullFacetValue] annotation.
 */
class IgnoreNullFacetValueAnnotationMirror() : AnnotationMirror(annotationClass = IgnoreNullFacetValue::class)