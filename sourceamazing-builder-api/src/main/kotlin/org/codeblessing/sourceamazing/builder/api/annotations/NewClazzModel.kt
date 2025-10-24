package org.codeblessing.sourceamazing.builder.api.annotations

import kotlin.reflect.KClass

/** To be read like: "Create a new empty model of type 'clazz' accessible with the alias 'alias'." */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class NewClazzModel(val clazz: KClass<*>, val alias: String)
