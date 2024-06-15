package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror

/**
 * Represents a [ExpectedAliasFromSuperiorBuilder] annotation.
 */
class ExpectedAliasFromSuperiorBuilderAnnotationMirror(val conceptAlias: String)
    : AnnotationMirror(annotationClass = ExpectedAliasFromSuperiorBuilder::class)