package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import kotlin.reflect.KClass

class QueryFacetValueAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = QueryFacetValue::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val queryFacetValueAnnotation = annotation as QueryFacetValue
        return QueryFacetValueAnnotationMirror(
            facetClass = classMirrorCreatorCallable.classMirrorCreator(queryFacetValueAnnotation.facetClass),
        )
    }
}