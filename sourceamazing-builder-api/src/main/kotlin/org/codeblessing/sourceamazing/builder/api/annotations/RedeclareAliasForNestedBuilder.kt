package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * To be read like: "In the subordinated builder that will be called with or returned by this method, let the clazz
 * model with the alias 'alias' be accessible with another alias 'newAlias'."
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class RedeclareAliasForNestedBuilder(val alias: String, val newAlias: String)
