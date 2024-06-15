package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror

/**
 * Represents a [BuilderMethod] annotation.
 */
class BuilderMethodAnnotationMirror() : AnnotationMirror(annotationClass = BuilderMethod::class)