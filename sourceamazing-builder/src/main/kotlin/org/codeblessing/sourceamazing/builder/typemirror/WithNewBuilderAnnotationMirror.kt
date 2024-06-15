package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

/**
 * Represents a [WithNewBuilder] annotation.
 */
class WithNewBuilderAnnotationMirror(val builderClass: MirrorProvider<ClassMirrorInterface>)
    : AnnotationMirror(annotationClass = WithNewBuilder::class)