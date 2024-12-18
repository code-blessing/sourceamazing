package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * Marks a function parameter that provides a data instance.
 *
 * Data instances are real class instances. They must be
 * annotated with [BuilderData] and all methods and fields
 * that are annotated with [BuilderDataProvider] will be
 * executed and the provided data is used to create
 * new concepts and set facet values, similar to
 * the builder method parameters.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProvideBuilderData
