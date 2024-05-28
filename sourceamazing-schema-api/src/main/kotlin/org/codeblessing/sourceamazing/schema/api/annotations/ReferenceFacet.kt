package org.codeblessing.sourceamazing.schema.api.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ReferenceFacet(
    val referencedConcepts: Array<KClass<*>>,
    val minimumOccurrences: Int = 1,
    val maximumOccurrences: Int = 1,
)
