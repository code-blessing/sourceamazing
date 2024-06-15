package org.codeblessing.sourceamazing.schema.type

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations

fun Annotation.isAnnotationFromSourceAmazing(): Boolean {
    return this.annotationClass.packageName.startsWith("org.codeblessing")
}


inline fun <reified T: Annotation> KAnnotatedElement.getAnnotation(): T = getAnnotation(T::class)

fun <T: Annotation> KAnnotatedElement.getAnnotation(clazz: KClass<T>): T = findAnnotations(clazz).first()

fun KAnnotatedElement.hasAnnotation(clazz: KClass<out Annotation>): Boolean = findAnnotations(clazz).any()