package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import kotlin.reflect.KClass

class QueryConceptsAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = QueryConcepts::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val queryConceptsAnnotation = annotation as QueryConcepts
        return QueryConceptsAnnotationMirror(
            concepts = queryConceptsAnnotation.conceptClasses.map { classMirrorCreatorCallable.classMirrorCreator(it) },
        )
    }
}