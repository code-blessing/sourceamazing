package org.codeblessing.sourceamazing.builder.api.annotations


/**
 * Marks a data builder interface.
 * The methods of a builder (annotated with [BuilderMethod])
 * receive data as method value parameters.
 * This data is used to create new concepts and set facet
 * values.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Builder
