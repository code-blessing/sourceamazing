package org.codeblessing.sourceamazing.schema.typemirror

import kotlin.reflect.KClass

abstract class AbstractMirror(
){
    abstract val annotations: List<AnnotationMirror>

    fun longText(): String {
        TODO()
    }

    fun shortText(): String {
        TODO()
    }

    @Deprecated("Use only mirrors")
    fun hasAnnotation(annotation: KClass<out Annotation>): Boolean {
        TODO()
    }

    fun hasAnnotationMirror(annotationMirrorClass: KClass<out AnnotationMirror>): Boolean {
        TODO()
    }

    fun <T: AnnotationMirror> getAnnotationMirrorOrNull(annotationMirrorClass: KClass<out T>): T? {
        return annotations.filterIsInstance(annotationMirrorClass.java).firstOrNull()
    }

    fun <T: AnnotationMirror> getAnnotationMirror(annotationMirrorClass: KClass<out T>): T {
        return annotations.filterIsInstance(annotationMirrorClass.java).first()
    }

    fun <T: AnnotationMirror> getAnnotationMirrors(annotationMirrorClass: KClass<out T>): List<T> {
        return annotations.filterIsInstance(annotationMirrorClass.java)
    }
}