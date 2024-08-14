package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import kotlin.reflect.KClass

class BooleanFacetAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = BooleanFacet::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val facetAnnotation = annotation as BooleanFacet
        return BooleanFacetAnnotationMirror(
            minimumOccurrences = facetAnnotation.minimumOccurrences,
            maximumOccurrences = facetAnnotation.maximumOccurrences,
        )
    }
}