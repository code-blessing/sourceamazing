package org.codeblessing.sourceamazing.schema.typemirror

import kotlin.reflect.KClass

abstract class AbstractMirror(
){
    abstract val annotations: List<AnnotationMirror>

    fun hasAnnotation(annotation: KClass<out Annotation>): Boolean {
        return annotations.any { it.isAnnotation(annotation) }
    }

    fun numberOfAnnotation(annotation: KClass<out Annotation>): Int {
        return annotations.filter { it.isAnnotation(annotation) }.size
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