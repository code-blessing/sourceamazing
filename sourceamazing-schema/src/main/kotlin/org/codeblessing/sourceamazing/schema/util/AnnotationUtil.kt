package org.codeblessing.sourceamazing.schema.util

import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.reflect.KClass

object AnnotationUtil {

    fun hasAnnotation(classWithAnnotation: KClass<*>, annotation: KClass<out Annotation>): Boolean {
        return classWithAnnotation.java.getAnnotation(annotation.java) != null
    }

    fun <A: Annotation> getAnnotation(classWithAnnotation: KClass<*>, annotation: KClass<A>): A {
        return classWithAnnotation.java.getAnnotation(annotation.java)
    }

    fun <A: Annotation> getAnnotations(classWithAnnotation: KClass<*>, annotation: KClass<A>): List<A> {
        return classWithAnnotation.java.getAnnotationsByType(annotation.java).toList()
    }

    fun hasAnnotation(methodWithAnnotation: Method, annotation: KClass<out Annotation>): Boolean {
        return methodWithAnnotation.isAnnotationPresent(annotation.java)
    }

    fun <A: Annotation> getAnnotation(methodWithAnnotation: Method, annotation: KClass<A>): A {
        return methodWithAnnotation.getAnnotation(annotation.java)
    }

    fun <A: Annotation> getAnnotations(methodWithAnnotation: Method, annotation: KClass<A>): List<A> {
        return methodWithAnnotation.getAnnotationsByType(annotation.java).toList()
    }

    fun <A: Annotation> getAnnotations(methodParamWithAnnotation: Parameter, annotation: KClass<A>): List<A> {
        return methodParamWithAnnotation.getAnnotationsByType(annotation.java).toList()
    }

    fun hasAnnotation(methodParamWithAnnotation: Parameter, annotation: KClass<out Annotation>): Boolean {
        return methodParamWithAnnotation.isAnnotationPresent(annotation.java)
    }

    fun <A: Annotation> getAnnotation(methodParamWithAnnotation: Parameter, annotation: KClass<A>): A {
        return methodParamWithAnnotation.getAnnotation(annotation.java)
    }

}