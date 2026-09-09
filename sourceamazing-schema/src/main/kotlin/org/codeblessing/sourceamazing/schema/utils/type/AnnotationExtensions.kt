package org.codeblessing.sourceamazing.schema.utils.type

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations

fun Annotation.isAnnotationFromSourceAmazing(): Boolean = this.annotationClass.packageName.startsWith("org.codeblessing.sourceamazing")

inline fun <reified T : Annotation> KAnnotatedElement.getAnnotation(): T = getAnnotation(T::class)

fun <T : Annotation> KAnnotatedElement.getAnnotation(clazz: KClass<T>): T = findAnnotations(clazz).first()

fun KAnnotatedElement.hasAnnotation(clazz: KClass<out Annotation>): Boolean = findAnnotations(clazz).any()
