package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror

/**
 * Represents a [Builder] annotation.
 */
class BuilderAnnotationMirror() : AnnotationMirror(annotationClass = Builder::class)