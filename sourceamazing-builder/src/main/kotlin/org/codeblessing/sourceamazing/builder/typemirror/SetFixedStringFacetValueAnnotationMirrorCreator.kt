package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedStringFacetValue
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirrorCreator
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorCreatorCallable
import kotlin.reflect.KClass

class SetFixedStringFacetValueAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = SetFixedStringFacetValue::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val setFacetValueAnnotation = annotation as SetFixedStringFacetValue
        return SetFixedStringFacetValueAnnotationMirror(
            conceptToModifyAlias = setFacetValueAnnotation.conceptToModifyAlias,
            facetToModify = classMirrorCreatorCallable.classMirrorCreator(setFacetValueAnnotation.facetToModify),
            facetModificationRule = setFacetValueAnnotation.facetModificationRule,
            value = setFacetValueAnnotation.value,
        )

    }
}