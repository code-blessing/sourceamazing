package org.codeblessing.sourceamazing.schema.typemirror

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER
    /*AnnotationTarget.EXPRESSION*/)
@Retention(AnnotationRetention.RUNTIME)
annotation class EveryLocationAnnotation
