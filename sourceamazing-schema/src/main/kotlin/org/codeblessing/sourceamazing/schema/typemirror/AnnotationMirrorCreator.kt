package org.codeblessing.sourceamazing.schema.typemirror

import kotlin.reflect.KClass

interface AnnotationMirrorCreator {
    fun annotationClass(): KClass<out Annotation>

    fun createAnnotationMirror(annotation: Annotation, classMirrorCreatorCallable: ClassMirrorCreatorCallable): AnnotationMirror
}