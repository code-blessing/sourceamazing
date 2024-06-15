package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

/**
 * Represents a [QueryConcepts] annotation.
 */
class QueryConceptsAnnotationMirror(val concepts: List<MirrorProvider<ClassMirrorInterface>>)
    : AnnotationMirror(annotationClass = QueryConcepts::class)