package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

/**
 * Represents a [Concept] annotation.
 */
class ConceptAnnotationMirror(val facets: List<MirrorProvider<ClassMirrorInterface>>)
    : AnnotationMirror(annotationClass = Concept::class)