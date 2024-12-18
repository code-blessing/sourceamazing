package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * Using this annotation, passing a null-value will be ignored.
 * Without this annotation, an exception is thrown.
 *
 * This annotation can only be used together with
 * [SetProvidedFacetValue] on a method of a
 * [BuilderDataProvider].
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreProvidedNullFacetValue
