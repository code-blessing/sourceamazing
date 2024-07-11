package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts

/**
 * Represents a [QueryConcepts] annotation.
 */
class QueryConceptsAnnotationMirror(val concepts: List<ClassMirror>) : AnnotationMirror