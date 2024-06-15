package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror


/**
 * Represents a [InjectBuilder] annotation.
 */
class InjectBuilderAnnotationMirror() : AnnotationMirror(annotationClass = InjectBuilder::class)