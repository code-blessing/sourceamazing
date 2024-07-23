package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.DEFAULT_CONCEPT_ALIAS
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedBooleanFacetValue
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

/**
 * Represents a [SetFixedBooleanFacetValue] annotation.
 */
class SetFixedBooleanFacetValueAnnotationMirror(
    conceptToModifyAlias: String = DEFAULT_CONCEPT_ALIAS,
    facetToModify: MirrorProvider<ClassMirror>,
    facetModificationRule: FacetModificationRule = FacetModificationRule.ADD,
    val value: Boolean,
) : AbstractSetFixedFacetValueAnnotationMirror(
    annotationClass = SetFixedBooleanFacetValue::class,
    conceptToModifyAlias = conceptToModifyAlias,
    facetToModify = facetToModify,
    facetModificationRule = facetModificationRule
)

