package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import kotlin.reflect.KClass

class IntFacetAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = IntFacet::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val facetAnnotation = annotation as IntFacet
        return IntFacetAnnotationMirror(
            minimumOccurrences = facetAnnotation.minimumOccurrences,
            maximumOccurrences = facetAnnotation.maximumOccurrences,
        )
    }
}