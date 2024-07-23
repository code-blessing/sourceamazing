package org.codeblessing.sourceamazing.schema.typemirror

import kotlin.reflect.KClass

abstract class AbstractMirror(
){
    abstract val annotations: List<AnnotationMirror>

    abstract fun longText(): String

    abstract fun shortText(): String

    fun hasAnnotation(annotation: KClass<out Annotation>): Boolean {
        return annotations.any { it.isAnnotation(annotation) }
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