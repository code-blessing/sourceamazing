package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import kotlin.reflect.KClass

class EnumFacetAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = EnumFacet::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val facetAnnotation = annotation as EnumFacet
        return EnumFacetAnnotationMirror(
            minimumOccurrences = facetAnnotation.minimumOccurrences,
            maximumOccurrences = facetAnnotation.maximumOccurrences,
            enumerationClass = classMirrorCreatorCallable.classMirrorCreator(facetAnnotation.enumerationClass)
        )
    }
}