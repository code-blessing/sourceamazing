package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.provider.ClassMirrorProvider

/**
 * Represents a [NewConcept] annotation.
 */
class NewConceptAnnotationMirror(val concept: ClassMirrorProvider, val declareConceptAlias: String) : AnnotationMirror