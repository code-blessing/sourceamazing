package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.DEFAULT_CONCEPT_ALIAS
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

/**
 * Represents a [SetFacetValue] annotation.
 */
class SetFacetValueAnnotationMirror(
    conceptToModifyAlias: String = DEFAULT_CONCEPT_ALIAS,
    facetToModify: MirrorProvider<ClassMirrorInterface>,
    facetModificationRule: FacetModificationRule = FacetModificationRule.ADD,
) : AbstractSetFixedFacetValueAnnotationMirror(
    annotationClass = SetFacetValue::class,
    conceptToModifyAlias = conceptToModifyAlias,
    facetToModify = facetToModify,
    facetModificationRule = facetModificationRule
)