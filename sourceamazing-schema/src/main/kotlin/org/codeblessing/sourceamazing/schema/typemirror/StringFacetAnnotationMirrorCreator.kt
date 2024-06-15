package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import kotlin.reflect.KClass

class StringFacetAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = StringFacet::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val facetAnnotation = annotation as StringFacet
        return StringFacetAnnotationMirror(
            minimumOccurrences = facetAnnotation.minimumOccurrences,
            maximumOccurrences = facetAnnotation.maximumOccurrences,
        )
    }
}