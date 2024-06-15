package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.DEFAULT_CONCEPT_ALIAS
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

/**
 * Represents a [NewConcept] annotation.
 */
class NewConceptAnnotationMirror(val concept: MirrorProvider<ClassMirrorInterface>, val declareConceptAlias: String = DEFAULT_CONCEPT_ALIAS)
    : AnnotationMirror(annotationClass = NewConcept::class)