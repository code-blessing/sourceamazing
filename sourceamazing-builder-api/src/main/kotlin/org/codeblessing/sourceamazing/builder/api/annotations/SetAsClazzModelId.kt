package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * To be read like: "Use the passed value as the concept identifier for the clazz model that is accessible with the
 * alias 'alias'." (The passed value is the method argument of the method parameter that is annotated by this
 * annotation)
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SetAsClazzModelId(val alias: String)
