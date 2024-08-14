package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirrorCreator
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorCreatorCallable
import kotlin.reflect.KClass

class SetAliasConceptIdentifierValueAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = SetAliasConceptIdentifierReferenceFacetValue::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val setAliasConceptIdReferenceFacetValueAnnotation = annotation as SetAliasConceptIdentifierReferenceFacetValue
        return SetAliasConceptIdentifierReferenceFacetValueAnnotationMirror(
            conceptToModifyAlias = setAliasConceptIdReferenceFacetValueAnnotation.conceptToModifyAlias,
            facetToModify = classMirrorCreatorCallable.classMirrorCreator(setAliasConceptIdReferenceFacetValueAnnotation.facetToModify),
            facetModificationRule = setAliasConceptIdReferenceFacetValueAnnotation.facetModificationRule,
            referencedConceptAlias = setAliasConceptIdReferenceFacetValueAnnotation.referencedConceptAlias,
        )

    }
}