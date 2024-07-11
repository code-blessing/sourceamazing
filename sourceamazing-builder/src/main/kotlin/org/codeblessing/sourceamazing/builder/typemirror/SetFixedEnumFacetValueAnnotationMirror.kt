package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.DEFAULT_CONCEPT_ALIAS
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedEnumFacetValue
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror

/**
 * Represents a [SetFixedEnumFacetValue] annotation.
 */
class SetFixedEnumFacetValueAnnotationMirror(
    val conceptToModifyAlias: String = DEFAULT_CONCEPT_ALIAS,
    val facetToModify: ClassMirror,
    val facetModificationRule: FacetModificationRule,
    val value: String, // TODO Might be a Enum value
) : AnnotationMirror

