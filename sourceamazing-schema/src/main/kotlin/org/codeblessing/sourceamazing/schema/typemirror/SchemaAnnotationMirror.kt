package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

/**
 * Represents a [Schema] annotation.
 */
data class SchemaAnnotationMirror(val concepts: List<MirrorProvider<ClassMirrorInterface>>)
    : AnnotationMirror(annotationClass = Schema::class)