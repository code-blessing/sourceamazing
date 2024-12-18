package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * Marks a function parameter to which a new (Sub-)builder
 * will be injected into.
 *
 * The parameter type must be '<Builder-Interface>.() -> Unit'
 * where Builder-Interface is an interface from which a builder
 * is created and then injected as value parameter.
 * This builder interface must conform all constraints that are
 * given for builder interface like the need to be annotated
 * with [Builder] etc.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectBuilder
