package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import kotlin.reflect.KClass

class ReferenceFacetAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = ReferenceFacet::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val facetAnnotation = annotation as ReferenceFacet
        return ReferenceFacetAnnotationMirror(
            minimumOccurrences = facetAnnotation.minimumOccurrences,
            maximumOccurrences = facetAnnotation.maximumOccurrences,
            referencedConcepts = facetAnnotation.referencedConcepts.map { classMirrorCreatorCallable.classMirrorCreator(it) }
        )
    }
}