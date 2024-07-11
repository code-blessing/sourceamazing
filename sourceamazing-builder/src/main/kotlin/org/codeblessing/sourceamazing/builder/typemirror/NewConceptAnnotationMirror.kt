package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror

/**
 * Represents a [NewConcept] annotation.
 */
class NewConceptAnnotationMirror(val concept: ClassMirror, val declareConceptAlias: String) : AnnotationMirror