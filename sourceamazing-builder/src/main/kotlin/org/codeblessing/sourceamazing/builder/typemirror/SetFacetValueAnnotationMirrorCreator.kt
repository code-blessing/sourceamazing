package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirrorCreator
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorCreatorCallable
import kotlin.reflect.KClass

class SetFacetValueAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = SetFacetValue::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val setFacetValueAnnotation = annotation as SetFacetValue
        return SetFacetValueAnnotationMirror(
            conceptToModifyAlias = setFacetValueAnnotation.conceptToModifyAlias,
            facetToModify = classMirrorCreatorCallable.classMirrorCreator(setFacetValueAnnotation.facetToModify),
            facetModificationRule = setFacetValueAnnotation.facetModificationRule,
        )

    }
}