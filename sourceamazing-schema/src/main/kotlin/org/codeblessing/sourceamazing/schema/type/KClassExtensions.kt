package org.codeblessing.sourceamazing.schema.type

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations

val KClass<*>.isInterface: Boolean
    get() = this.toKClassJavaCompatibilityLayer().isInterface

val KClass<*>.isEnum: Boolean
    get() = this.toKClassJavaCompatibilityLayer().isEnum

val KClass<*>.isAnnotation: Boolean
    get() = this.toKClassJavaCompatibilityLayer().isAnnotation

val KClass<*>.isRegularClass: Boolean
    get() = this.toKClassJavaCompatibilityLayer().isRegularClass

val  KClass<*>.enumValues: List<String>
    get() = this.toKClassJavaCompatibilityLayer().enumValues

inline fun <reified T: Annotation> KAnnotatedElement.getAnnotation(): T = getAnnotation(T::class)

fun <T: Annotation> KAnnotatedElement.getAnnotation(clazz: KClass<T>): T = findAnnotations(clazz).first()

fun KAnnotatedElement.hasAnnotation(clazz: KClass<out Annotation>): Boolean = findAnnotations(clazz).any()

fun KAnnotatedElement.getNumberOfAnnotation(clazz: KClass<out Annotation>): Int = findAnnotations(clazz).count()


val KClass<*>.packageName: String
    get() = ClassNameUtil.packageFromQualifiedName(this.qualifiedName)

val KClass<*>.className: String
    get() = this.simpleName ?: ""

val KClass<*>.fullQualifiedName: String
    get() = ClassNameUtil.fullQualifiedName(packageName, this.simpleName) ?: ""

private fun KClass<*>.toKClassJavaCompatibilityLayer(): KClassJavaCompatibilityLayer {
    return if (this is KClassJavaCompatibilityLayer) {
        this
    } else {
        KClassJavaCompatibilityLayerImpl(this)
    }
}

private class KClassJavaCompatibilityLayerImpl(kClass: KClass<*>) : KClassJavaCompatibilityLayer {
    override val enumValues: List<String> = kClass.java.enumConstants.map { it.toString() }.toList()
    override val isAnnotation: Boolean = kClass.java.isAnnotation
    override val isInterface: Boolean = kClass.java.isInterface && !this.isAnnotation
    override val isEnum: Boolean = kClass.java.isEnum
    override val isRegularClass: Boolean = !isInterface && !isEnum && !isAnnotation
}
