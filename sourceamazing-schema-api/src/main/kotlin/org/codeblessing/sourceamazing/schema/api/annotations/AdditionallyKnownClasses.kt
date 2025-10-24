package org.codeblessing.sourceamazing.schema.api.annotations

import kotlin.reflect.KClass

/**
 * Make this classes known by the schema processor. This is especially useful if the schema has only declared an
 * interface but implementations of this interface are passed in.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AdditionallyKnownClasses(val classes: Array<KClass<*>>)
