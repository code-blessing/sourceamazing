package org.codeblessing.sourceamazing.schema.type

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations

val KClass<*>.isInterface: Boolean
    get() = java.isInterface && !this.isAnnotation

val KClass<*>.isEnum: Boolean
    get() = java.isEnum

val KClass<*>.isAnnotation: Boolean
    get() = java.isAnnotation

val KClass<*>.isRegularClass: Boolean
    get() = !isInterface && !isEnum && !isAnnotation

val  KClass<*>.enumValues: List<Enum<*>>
    get() = java.enumConstants?.map { it as Enum<*> }?.toList() ?: emptyList()

inline fun <reified T: Annotation> KAnnotatedElement.getAnnotation(): T = getAnnotation(T::class)

fun <T: Annotation> KAnnotatedElement.getAnnotation(clazz: KClass<T>): T = findAnnotations(clazz).first()

fun KAnnotatedElement.hasAnnotation(clazz: KClass<out Annotation>): Boolean = findAnnotations(clazz).any()

fun KAnnotatedElement.getNumberOfAnnotation(clazz: KClass<out Annotation>): Int = findAnnotations(clazz).count()


val KClass<*>.packageName: String
    get() = ClassNameUtil.packageFromQualifiedName(this.qualifiedName)