package org.codeblessing.sourceamazing.api.process.schema.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Facet(
    val type: FacetType,
    val minimumOccurrences: Int = 1,
    val maximumOccurrences: Int = 1,
    val enumerationClass: KClass<*> = Unit::class,
    val referencedConcepts: Array<KClass<*>> = []
)
