package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror

/**
 * Represents a [SetConceptIdentifierValue] annotation.
 */
class SetConceptIdentifierValueAnnotationMirror(val conceptToModifyAlias: String) : AnnotationMirror