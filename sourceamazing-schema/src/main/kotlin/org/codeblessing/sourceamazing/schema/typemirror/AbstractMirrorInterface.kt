package org.codeblessing.sourceamazing.schema.typemirror

import kotlin.reflect.KClass

interface AbstractMirrorInterface {
    val annotations: List<AnnotationMirror>

    fun longText(): String

    fun shortText(): String

    fun hasAnnotation(annotation: KClass<out Annotation>): Boolean

    fun numberOfAnnotation(annotation: KClass<out Annotation>): Int

    fun <T: AnnotationMirror> getAnnotationMirrorOrNull(annotationMirrorClass: KClass<out T>): T?

    fun <T: AnnotationMirror> getAnnotationMirror(annotationMirrorClass: KClass<out T>): T

    fun <T: AnnotationMirror> getAnnotationMirrors(annotationMirrorClass: KClass<out T>): List<T>
}