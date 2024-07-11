package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.Concept

/**
 * Represents a [Concept] annotation.
 */
class ConceptAnnotationMirror(val facets: Collection<ClassMirror>) : AnnotationMirror