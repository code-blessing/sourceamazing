package org.codeblessing.sourceamazing.schema.type

import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.superclasses

val  KClass<*>.classAndSuperclasses: List<KClass<*>>
    get() = listOf(this) + this.superclasses

val KClass<*>.annotationsIncludingSuperclasses: List<Annotation>
    get() = classAndSuperclasses.map { it.annotations }.flatten()

private fun <T: Annotation> findAnnotationIncludingSuperclasses(annotation: KClass<T>, clazz: KClass<*>): List<T> {
    return clazz.classAndSuperclasses.map { it.findAnnotations(annotation)}.flatten()
}

inline fun <reified T: Annotation> KClass<*>.getAnnotationIncludingSuperclasses(): T = getAnnotationIncludingSuperclasses(T::class)

fun <T: Annotation> KClass<*>.getAnnotationIncludingSuperclasses(annotationClass: KClass<T>): T = findAnnotationIncludingSuperclasses(annotationClass, this).first()

fun KClass<*>.hasAnnotationIncludingSuperclasses(annotationClass: KClass<out Annotation>): Boolean = findAnnotationIncludingSuperclasses(annotationClass, this).any()

fun KClass<*>.getNumberOfAnnotationIncludingSuperclasses(annotationClass: KClass<out Annotation>): Int = findAnnotationIncludingSuperclasses(annotationClass, this).count()


val KClass<*>.isInterface: Boolean
    get() = java.isInterface && !this.isAnnotation

val KClass<*>.isEnum: Boolean
    get() = java.isEnum

val KClass<*>.isPrivate: Boolean
    get() = Modifier.isPrivate(java.modifiers)

val KClass<*>.isAnnotation: Boolean
    get() = java.isAnnotation

val KClass<*>.isRegularClass: Boolean
    get() = !isInterface && !isEnum && !isAnnotation

val  KClass<*>.enumValues: List<Enum<*>>
    get() = java.enumConstants?.filterIsInstance<Enum<*>>()?.toList() ?: emptyList()

val KClass<*>.packageName: String
    get() = ClassNameUtil.packageFromQualifiedName(this.qualifiedName)