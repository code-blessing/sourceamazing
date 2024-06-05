package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * Using this annotation, passing a null-value will be ignored.
 * Without this annotation, an exception is thrown.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreNullFacetValue
