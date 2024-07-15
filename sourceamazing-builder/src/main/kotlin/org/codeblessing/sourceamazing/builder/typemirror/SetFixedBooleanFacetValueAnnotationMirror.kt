package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.DEFAULT_CONCEPT_ALIAS
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedBooleanFacetValue
import org.codeblessing.sourceamazing.schema.typemirror.provider.ClassMirrorProvider

/**
 * Represents a [SetFixedBooleanFacetValue] annotation.
 */
class SetFixedBooleanFacetValueAnnotationMirror(
    conceptToModifyAlias: String = DEFAULT_CONCEPT_ALIAS,
    facetToModify: ClassMirrorProvider,
    facetModificationRule: FacetModificationRule,
    val value: Boolean,
) : AbstractSetFixedFacetValueAnnotationMirror(
    conceptToModifyAlias = conceptToModifyAlias,
    facetToModify = facetToModify,
    facetModificationRule = facetModificationRule
)

