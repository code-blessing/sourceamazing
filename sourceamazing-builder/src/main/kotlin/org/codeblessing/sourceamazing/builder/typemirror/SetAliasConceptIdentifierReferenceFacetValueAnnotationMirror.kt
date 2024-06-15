package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.DEFAULT_CONCEPT_ALIAS
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

/**
 * Represents a [SetAliasConceptIdentifierReferenceFacetValue] annotation.
 */
class SetAliasConceptIdentifierReferenceFacetValueAnnotationMirror(
    val conceptToModifyAlias: String = DEFAULT_CONCEPT_ALIAS,
    val facetToModify: MirrorProvider<ClassMirrorInterface>,
    val facetModificationRule: FacetModificationRule = FacetModificationRule.ADD,
    val referencedConceptAlias: String,
) : AnnotationMirror(annotationClass = SetAliasConceptIdentifierReferenceFacetValue::class)

