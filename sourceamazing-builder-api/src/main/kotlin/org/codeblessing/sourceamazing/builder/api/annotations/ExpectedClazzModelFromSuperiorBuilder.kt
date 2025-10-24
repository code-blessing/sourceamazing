package org.codeblessing.sourceamazing.builder.api.annotations

import kotlin.reflect.KClass

/**
 * To be read like: "I expect a clazz model for the type 'clazz' that has been declared with the alias 'alias' from the
 * superior builder that called this builder ."
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class ExpectedClazzModelFromSuperiorBuilder(val clazz: KClass<*>, val alias: String)
