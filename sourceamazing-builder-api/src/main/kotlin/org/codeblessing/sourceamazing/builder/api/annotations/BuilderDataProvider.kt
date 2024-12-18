package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * Marks a data instances.
 * Data instances are real class instances. They must be
 * annotated with [BuilderDataProvider] and all methods and fields
 * that are annotated with [BuilderData] will be
 * executed and the provided data is used to create
 * new concepts and set facet values, similar to
 * the builder method parameters.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class BuilderDataProvider
