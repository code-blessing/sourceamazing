package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.Schema

/**
 * Represents a [Schema] annotation.
 */
data class SchemaAnnotationMirror(val concepts: List<ClassMirror>): AnnotationMirror