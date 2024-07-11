package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror

/**
 * Represents a [WithNewBuilder] annotation.
 */
class WithNewBuilderAnnotationMirror(val builderClass: ClassMirror) : AnnotationMirror