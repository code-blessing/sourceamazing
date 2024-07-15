package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.typemirror.provider.ClassMirrorProvider

/**
 * Represents a [Concept] annotation.
 */
class ConceptAnnotationMirror(val facets: List<ClassMirrorProvider>) : AnnotationMirror