package org.codeblessing.sourceamazing.schema.typemirror

import kotlin.reflect.KClass

abstract class AnnotationMirror(val annotationClass: KClass<out Annotation>) {
    fun isAnnotation(annotationClassToCompare: KClass<out Annotation>): Boolean {
        return annotationClass == annotationClassToCompare
    }

}