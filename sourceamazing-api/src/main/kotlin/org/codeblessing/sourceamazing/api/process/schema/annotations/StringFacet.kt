package org.codeblessing.sourceamazing.api.process.schema.annotations


@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class StringFacet(
    val minimumOccurrences: Int = 1,
    val maximumOccurrences: Int = 1,
)
