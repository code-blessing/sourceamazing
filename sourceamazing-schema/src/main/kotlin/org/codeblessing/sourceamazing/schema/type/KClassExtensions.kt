package org.codeblessing.sourceamazing.schema.type

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations

val KClass<*>.isInterface: Boolean
    get() = this.java.isInterface && !this.isAnnotation

val KClass<*>.isEnum: Boolean
    get() = this.java.isEnum

val KClass<*>.isAnnotation: Boolean
    get() = this.java.isAnnotation

val KClass<*>.isRegularClass: Boolean
    get() = !isInterface && !isEnum && !isAnnotation


val  KClass<*>.enumValues: List<String>
    get() = this.java.enumConstants.map { it.toString() }.toList()


inline fun <reified T: Annotation> KAnnotatedElement.getAnnotation(): T = getAnnotation(T::class)

fun <T: Annotation> KAnnotatedElement.getAnnotation(clazz: KClass<T>): T = findAnnotations(clazz).first()

fun KAnnotatedElement.hasAnnotation(clazz: KClass<out Annotation>): Boolean = findAnnotations(clazz).any()

fun KAnnotatedElement.getNumberOfAnnotation(clazz: KClass<out Annotation>): Int = findAnnotations(clazz).count()

// see https://youtrack.jetbrains.com/issue/KT-18104
val KClass<*>.packageName: String
    get() = this.qualifiedName?.split(".")?.dropLast(1)?.joinToString(".") ?: ""

val KClass<*>.className: String
    get() = this.simpleName ?: ""

val KClass<*>.fullQualifiedName: String
    get() = if(packageName.isNotEmpty()) "$packageName.$className" else className

