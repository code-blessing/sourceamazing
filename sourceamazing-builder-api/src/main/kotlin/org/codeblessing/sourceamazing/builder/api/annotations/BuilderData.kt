package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * Marks a method or a field that provides data.
 * All methods and fields that are annotated will be
 * executed and the provided data is used to create
 * new concepts and set facet values, similar to
 * the builder method parameters.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BuilderData
