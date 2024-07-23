package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror

/**
 * Represents a [SetRandomConceptIdentifierValue] annotation.
 */
class SetRandomConceptIdentifierValueAnnotationMirror(val conceptToModifyAlias: String)
    : AnnotationMirror(annotationClass = SetRandomConceptIdentifierValue::class)