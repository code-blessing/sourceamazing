package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import kotlin.reflect.KClass

class ConceptAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = Concept::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val conceptAnnotation = annotation as Concept
        return ConceptAnnotationMirror(conceptAnnotation.facets.map(classMirrorCreatorCallable::classMirrorCreator))

    }
}