package org.codeblessing.sourceamazing.schema.typemirror

import kotlin.reflect.KClass

abstract class AnnotationMirror(val annotationClass: KClass<out Annotation>) {
    fun isAnnotation(annotationClassToCompare: KClass<out Annotation>): Boolean {
        return annotationClass == annotationClassToCompare
    }

    open fun isAnnotationFromSourceAmazing(): Boolean {
        // as we only import source amazing annotations as mirrors,
        // this is always true
        return true
    }
}