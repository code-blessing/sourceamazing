package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.typemirror.provider.ClassMirrorProvider

/**
 * Represents a [Schema] annotation.
 */
data class SchemaAnnotationMirror(val concepts: List<ClassMirrorProvider>): AnnotationMirror