package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * Marks a method of a builder (annotated with [Builder]).
 * A method of a builder receive data as method value
 * parameters.
 * This data is used to create new concepts and set facet
 * values.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BuilderMethod
