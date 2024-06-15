package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedEnumFacetValue
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirrorCreator
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorCreatorCallable
import kotlin.reflect.KClass

class SetFixedEnumFacetValueAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = SetFixedEnumFacetValue::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val setFacetValueAnnotation = annotation as SetFixedEnumFacetValue
        return SetFixedEnumFacetValueAnnotationMirror(
            conceptToModifyAlias = setFacetValueAnnotation.conceptToModifyAlias,
            facetToModify = classMirrorCreatorCallable.classMirrorCreator(setFacetValueAnnotation.facetToModify),
            facetModificationRule = setFacetValueAnnotation.facetModificationRule,
            value = setFacetValueAnnotation.value,
        )

    }
}