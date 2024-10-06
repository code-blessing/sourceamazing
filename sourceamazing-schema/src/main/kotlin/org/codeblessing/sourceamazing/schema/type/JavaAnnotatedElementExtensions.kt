package org.codeblessing.sourceamazing.schema.type

import java.lang.reflect.AnnotatedElement

inline fun <reified T: Annotation> AnnotatedElement.getAnnotation(): T = annotations.first { it is T } as T

inline fun <reified T: Annotation> AnnotatedElement.hasAnnotation(): Boolean = annotations.any { it is T }

inline fun <reified T: Annotation> AnnotatedElement.findAnnotation(): T? = annotations.firstOrNull { it is T } as T?

inline fun <reified T: Annotation> AnnotatedElement.findAnnotations(): List<T> = annotations.filterIsInstance(T::class.java)


