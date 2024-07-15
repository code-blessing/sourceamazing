package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.provider.ClassMirrorProvider

/**
 * Represents a [WithNewBuilder] annotation.
 */
class WithNewBuilderAnnotationMirror(val builderClass: ClassMirrorProvider) : AnnotationMirror