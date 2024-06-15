package org.codeblessing.sourceamazing.schema.typemirror

import kotlin.reflect.KClass

class OtherAnnotationMirrorCreator(private val annotationClass: KClass<out Annotation>): AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = annotationClass

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        return OtherAnnotationMirror(
            annotationClass = annotationClass,
            annotation = annotation,
        )
    }
}